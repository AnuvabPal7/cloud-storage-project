package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    Optional<Folder> findByIdAndOwner(UUID id, User owner);

    List<Folder> findAllByOwnerAndParentIsNullOrderByNameAsc(User owner);

    List<Folder> findAllByOwnerAndParentOrderByNameAsc(User owner, Folder parent);

    // Used to block deleting a folder that still has subfolders in it.
    boolean existsByParent(Folder parent);
}
