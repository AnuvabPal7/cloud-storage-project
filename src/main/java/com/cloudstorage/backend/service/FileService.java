package com.cloudstorage.backend.service;

import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.FolderResponse;
import com.cloudstorage.backend.dto.SearchResponse;
import com.cloudstorage.backend.dto.StorageUsageResponse;
import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.repository.FileShareRepository;
import com.cloudstorage.backend.repository.FolderRepository;
import com.cloudstorage.backend.repository.PublicShareLinkRepository;
import com.cloudstorage.backend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final int SIGNED_URL_EXPIRY_SECONDS = 3600; // 1 hour

    // Allow-list of columns the client is allowed to sort by. Without this,
    // passing sortBy straight into Sort.by() lets a client probe for valid
    // entity property names via error messages, and any typo would 500
    // instead of just quietly falling back to a sane default.
    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "size", "createdAt");
    private static final int MAX_PAGE_SIZE = 100;

    // Matches Supabase's free-tier 1GB storage limit - see StorageUsageResponse
    // for why this is a per-user soft quota, not something Supabase itself enforces per user.
    private static final long STORAGE_LIMIT_BYTES = 1_073_741_824L; // 1 GB

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileShareRepository fileShareRepository;
    private final PublicShareLinkRepository publicShareLinkRepository;
    private final StorageService storageService;

    @Transactional
    public FileResponse upload(MultipartFile multipartFile, UUID folderId, User owner) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findByIdAndOwner(folderId, owner)
                    .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        }

        String safeName = sanitizeFilename(multipartFile.getOriginalFilename());

        // This is just for building a unique storage path - it does NOT have
        // to match the database's primary key. Keeping them separate avoids
        // a classic Spring Data JPA bug: if you manually assign @Id before
        // save(), Spring Data assumes the row already exists and calls
        // merge() (an UPDATE) instead of persist() (an INSERT) - which then
        // fails, since no such row exists yet. Letting Hibernate generate
        // the real ID during persist() avoids that entirely.
        UUID storageKey = UUID.randomUUID();
        String storagePath = owner.getId() + "/" + storageKey + "_" + safeName;

        storageService.upload(storagePath, multipartFile.getBytes(), multipartFile.getContentType());

        FileItem file = FileItem.builder()
                .owner(owner)
                .folder(folder) // null = root level
                .name(safeName)
                .storagePath(storagePath)
                .size(multipartFile.getSize())
                .mimeType(multipartFile.getContentType())
                .build();

        fileRepository.save(file);

        return toResponse(file, null);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listRootFiles(User owner) {
        return fileRepository.findAllByOwnerAndFolderIsNullAndDeletedFalseOrderByCreatedAtDesc(owner)
                .stream()
                .map(file -> toResponse(file, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public FileResponse getWithDownloadUrl(UUID fileId, User owner) throws IOException {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        String signedUrl = storageService.createSignedUrl(file.getStoragePath(), SIGNED_URL_EXPIRY_SECONDS);
        return toResponse(file, signedUrl);
    }

    /**
     * Like getWithDownloadUrl, but also allows access if the requesting
     * user isn't the owner but has a FileShare on this file (Day 5). This
     * is the actual "permission check" - the gate that decides whether a
     * Viewer/Editor can get a download link at all.
     *
     * Uses the same error message whether the file doesn't exist or the
     * user just lacks access, so the response can't be used to tell those
     * two cases apart - a small but genuine security-conscious choice.
     */
    @Transactional(readOnly = true)
    public FileResponse getSharedDownloadUrl(UUID fileId, User requestingUser) throws IOException {
        FileItem file = fileRepository.findById(fileId).orElse(null);

        boolean isOwner = file != null && file.getOwner().getId().equals(requestingUser.getId());
        boolean hasShare = file != null && !isOwner
                && fileShareRepository.findByFileAndSharedWithUser(file, requestingUser).isPresent();

        if (file == null || (!isOwner && !hasShare)) {
            throw new IllegalArgumentException("File not found or access denied");
        }

        String signedUrl = storageService.createSignedUrl(file.getStoragePath(), SIGNED_URL_EXPIRY_SECONDS);
        return toResponse(file, signedUrl);
    }

    @Transactional
    public FileResponse rename(UUID fileId, String newName, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        file.setName(newName);
        fileRepository.save(file);
        return toResponse(file, null);
    }

    /**
     * Moves a file between folders. Only changes the database record -
     * the file's actual bytes in Supabase Storage never move, since
     * "folder" here is purely an organizational concept in our schema,
     * not a real directory in the storage bucket.
     */
    @Transactional
    public FileResponse move(UUID fileId, UUID targetFolderId, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        Folder targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderRepository.findByIdAndOwner(targetFolderId, owner)
                    .orElseThrow(() -> new IllegalArgumentException("Target folder not found"));
        }

        file.setFolder(targetFolder); // null = move to root
        fileRepository.save(file);
        return toResponse(file, null);
    }

    // Soft delete: just flips a flag. The file stays in Supabase Storage
    // and in the database - nothing is actually destroyed yet. This is
    // what makes Trash/restore possible at all.
    @Transactional
    public void softDelete(UUID fileId, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        file.setDeleted(true);
        fileRepository.save(file);
    }

    @Transactional
    public FileResponse restore(UUID fileId, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        file.setDeleted(false);
        fileRepository.save(file);
        return toResponse(file, null);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> listTrash(User owner) {
        return fileRepository.findAllByOwnerAndDeletedTrueOrderByCreatedAtDesc(owner)
                .stream()
                .map(file -> toResponse(file, null))
                .toList();
    }

    /**
     * Global search across everything in the user's Drive - both files
     * AND folders, same as typing into Google Drive's search bar. `query`
     * matches folder/file names; `mimeType` is file-only (folders have no
     * mime type, so they're returned regardless of that filter).
     */
    @Transactional(readOnly = true)
    public SearchResponse search(String query, String mimeType, int page, int size,
                                  String sortBy, String sortDir, User owner) {
        String safeSortBy = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Guard against a client requesting page=-1 or size=10000 - keeps
        // every request cheap regardless of what gets passed in.
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        String normalizedQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        String normalizedMimeType = (mimeType != null && !mimeType.isBlank()) ? mimeType.trim() : null;

        Page<FileResponse> filePage = fileRepository.search(owner, normalizedQuery, normalizedMimeType, pageable)
                .map(file -> toResponse(file, null));

        List<FolderResponse> matchingFolders = folderRepository.search(owner, normalizedQuery).stream()
                .map(this::toFolderResponse)
                .toList();

        return new SearchResponse(matchingFolders, filePage);
    }

    private FolderResponse toFolderResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public StorageUsageResponse getStorageUsage(User owner) {
        long usedBytes = fileRepository.sumSizeByOwner(owner);
        return new StorageUsageResponse(usedBytes, STORAGE_LIMIT_BYTES);
    }

    /**
     * Actually deletes the file - from storage AND the database. Only
     * allowed on files already in Trash, so there's always a two-step
     * "soft delete, then permanently delete" path rather than one click
     * being able to destroy something unrecoverably by accident.
     *
     * Also cleans up any FileShare/PublicShareLink rows pointing at this
     * file first. Without this, deleting a file that's still shared with
     * someone (or has an active public link) hits a foreign key constraint
     * at the database level and fails with a raw 500 - those rows have no
     * meaning once the file itself is gone, so clearing them is correct,
     * not just a workaround.
     */
    @Transactional
    public void permanentlyDelete(UUID fileId, User owner) throws IOException {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (!file.isDeleted()) {
            throw new IllegalArgumentException("Move the file to Trash before permanently deleting it");
        }

        fileShareRepository.deleteAllByFile(file);
        publicShareLinkRepository.deleteAllByFile(file);

        storageService.delete(file.getStoragePath());
        fileRepository.delete(file);
    }

    private FileResponse toResponse(FileItem file, String downloadUrl) {
        return new FileResponse(
                file.getId(),
                file.getName(),
                file.getSize(),
                file.getMimeType(),
                file.getCreatedAt(),
                downloadUrl
        );
    }

    /**
     * Strips path separators and anything outside a safe character set.
     * Without this, a filename like "../../etc/passwd" could manipulate
     * where the file lands, and something like "<script>.js" could cause
     * problems if this name is ever rendered in a UI without escaping.
     */
    private String sanitizeFilename(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed_file";
        }
        String nameOnly = originalName.replaceAll("^.*[/\\\\]", ""); // strip any path prefix
        return nameOnly.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
