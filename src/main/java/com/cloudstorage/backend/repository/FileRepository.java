package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.Folder;
import com.cloudstorage.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Global search across all of a user's (non-trashed) files, regardless
     * of which folder they're in - same idea as Google Drive's search bar.
     * `query` and `mimeType` are both optional: the "(:x IS NULL OR ...)"
     * pattern means "skip this filter entirely if the parameter is null",
     * so the same query handles "search by name", "filter by type", or both
     * at once without needing several near-duplicate repository methods.
     *
     * The explicit CAST(:param AS string) is required for PostgreSQL:
     * without it, Postgres can't infer the parameter's type when it only
     * appears inside LOWER()/CONCAT() alongside an "IS NULL" check, and
     * throws "function lower(bytea) does not exist" at query time.
     */
    @Query("""
            SELECT f FROM FileItem f
            WHERE f.owner = :owner
              AND f.deleted = false
              AND (:query IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
              AND (:mimeType IS NULL OR f.mimeType LIKE CONCAT(CAST(:mimeType AS string), '%'))
            """)
    Page<FileItem> search(@Param("owner") User owner,
                           @Param("query") String query,
                           @Param("mimeType") String mimeType,
                           Pageable pageable);
}
