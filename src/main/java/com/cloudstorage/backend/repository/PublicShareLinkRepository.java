package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.PublicShareLink;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicShareLinkRepository extends JpaRepository<PublicShareLink, UUID> {

    Optional<PublicShareLink> findByToken(String token);

    Optional<PublicShareLink> findByIdAndFile_Owner(UUID id, User owner);

    List<PublicShareLink> findAllByFile(FileItem file);

    // Used when permanently deleting a file - clears any public links
    // pointing at it first, so the delete doesn't fail on a foreign key constraint.
    void deleteAllByFile(FileItem file);
}
