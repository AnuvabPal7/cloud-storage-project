package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    Optional<Folder> findByIdAndOwner(UUID id, User owner);

    List<Folder> findAllByOwnerAndParentIsNullOrderByNameAsc(User owner);

    List<Folder> findAllByOwnerAndParentOrderByNameAsc(User owner, Folder parent);

    // Used to block deleting a folder that still has subfolders in it.
    boolean existsByParent(Folder parent);

    /**
     * Folder search, kept unpaginated on purpose - a single user's folder
     * count is typically small (dozens, not thousands), so a plain list is
     * simpler and fine here even though file search (which can have far
     * more results) uses proper pagination.
     *
     * Same CAST(:query AS string) fix as FileRepository.search() - without
     * it, PostgreSQL can't infer the parameter's type when it only appears
     * inside LOWER()/CONCAT() alongside an "IS NULL" check.
     */
    @Query("""
            SELECT f FROM Folder f
            WHERE f.owner = :owner
              AND (:query IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
            ORDER BY f.name ASC
            """)
    List<Folder> search(@Param("owner") User owner, @Param("query") String query);
}
