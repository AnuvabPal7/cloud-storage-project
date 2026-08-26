package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.FileShare;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileShareRepository extends JpaRepository<FileShare, UUID> {

    Optional<FileShare> findByFileAndSharedWithUser(FileItem file, User sharedWithUser);

    Optional<FileShare> findByIdAndFile_Owner(UUID id, User owner);

    List<FileShare> findAllByFile(FileItem file);

    List<FileShare> findAllBySharedWithUserOrderByCreatedAtDesc(User sharedWithUser);
}
