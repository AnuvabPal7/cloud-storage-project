package com.cloudstorage.backend.service;

import com.cloudstorage.backend.dto.CreateFolderRequest;
import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.FolderContentsResponse;
import com.cloudstorage.backend.dto.FolderResponse;
import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    @Transactional
    public FolderResponse create(CreateFolderRequest request, User owner) {
        Folder parent = null;
        if (request.parentId() != null) {
            parent = folderRepository.findByIdAndOwner(request.parentId(), owner)
                    .orElseThrow(() -> new IllegalArgumentException("Parent folder not found"));
        }

        Folder folder = Folder.builder()
                .owner(owner)
                .parent(parent)
                .name(request.name())
                .build(); // no .id(...) here - same lesson as Day 3, let Hibernate generate it

        folderRepository.save(folder);
        return toResponse(folder);
    }

    /**
     * folderId == null means "show me the root" (top level of My Drive).
     * Otherwise, show that folder's immediate children - one level at a
     * time, same as clicking into a folder in Google Drive.
     */
    @Transactional(readOnly = true)
    public FolderContentsResponse getContents(UUID folderId, User owner) {
        Folder folder = null;
        List<Folder> subfolders;
        List<FileItem> files;

        if (folderId == null) {
            subfolders = folderRepository.findAllByOwnerAndParentIsNullOrderByNameAsc(owner);
            files = fileRepository.findAllByOwnerAndFolderIsNullAndDeletedFalseOrderByCreatedAtDesc(owner);
        } else {
            folder = folderRepository.findByIdAndOwner(folderId, owner)
                    .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
            subfolders = folderRepository.findAllByOwnerAndParentOrderByNameAsc(owner, folder);
            files = fileRepository.findAllByOwnerAndFolderAndDeletedFalseOrderByCreatedAtDesc(owner, folder);
        }

        List<FolderResponse> subfolderResponses = subfolders.stream().map(this::toResponse).toList();
        List<FileResponse> fileResponses = files.stream()
                .map(f -> new FileResponse(f.getId(), f.getName(), f.getSize(), f.getMimeType(), f.getCreatedAt(), null))
                .toList();

        return new FolderContentsResponse(
                folder != null ? toResponse(folder) : null,
                subfolderResponses,
                fileResponses
        );
    }

    @Transactional
    public FolderResponse rename(UUID folderId, String newName, User owner) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        folder.setName(newName);
        folderRepository.save(folder);
        return toResponse(folder);
    }

    /**
     * Deliberately refuses to delete a non-empty folder rather than silently
     * cascading (deleting everything inside it too). That's a safer default
     * for a portfolio project - it forces an explicit "empty it first"
     * step instead of one click nuking a whole folder tree by accident.
     */
    @Transactional
    public void delete(UUID folderId, User owner) {
        Folder folder = folderRepository.findByIdAndOwner(folderId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        boolean hasFiles = fileRepository.existsByFolder(folder);
        boolean hasSubfolders = folderRepository.existsByParent(folder);

        if (hasFiles || hasSubfolders) {
            throw new IllegalArgumentException("Folder is not empty. Move or delete its contents first.");
        }

        folderRepository.delete(folder);
    }

    private FolderResponse toResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt()
        );
    }
}
