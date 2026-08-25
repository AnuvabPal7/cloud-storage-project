package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileItem, UUID> {

    // Scoping every lookup to `owner` is what stops User A from fetching
    // User B's file just by guessing/incrementing an ID (an IDOR bug -
    // a classic real-world vulnerability, worth knowing the name of).
    Optional<FileItem> findByIdAndOwner(UUID id, User owner);

    List<FileItem> findAllByOwnerAndFolderIsNullAndDeletedFalseOrderByCreatedAtDesc(User owner);

    List<FileItem> findAllByOwnerAndFolderAndDeletedFalseOrderByCreatedAtDesc(User owner, Folder folder);

    List<FileItem> findAllByOwnerAndDeletedTrueOrderByCreatedAtDesc(User owner);

    // Used to block deleting a folder that still has files in it.
    boolean existsByFolder(Folder folder);
}
