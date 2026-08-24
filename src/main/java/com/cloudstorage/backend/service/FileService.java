package com.cloudstorage.backend.service;

import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final int SIGNED_URL_EXPIRY_SECONDS = 3600; // 1 hour

    private final FileRepository fileRepository;
    private final StorageService storageService;

    public FileResponse upload(MultipartFile multipartFile, User owner) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
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
                .folder(null) // root level for now - Day 4 adds folder targeting
                .name(safeName)
                .storagePath(storagePath)
                .size(multipartFile.getSize())
                .mimeType(multipartFile.getContentType())
                .build();

        fileRepository.save(file);

        return toResponse(file, null);
    }

    public List<FileResponse> listRootFiles(User owner) {
        return fileRepository.findAllByOwnerAndFolderIsNullAndDeletedFalseOrderByCreatedAtDesc(owner)
                .stream()
                .map(file -> toResponse(file, null))
                .toList();
    }

    public FileResponse getWithDownloadUrl(UUID fileId, User owner) throws IOException {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        String signedUrl = storageService.createSignedUrl(file.getStoragePath(), SIGNED_URL_EXPIRY_SECONDS);
        return toResponse(file, signedUrl);
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