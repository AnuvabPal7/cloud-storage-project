package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * A direct share: "this file is shared with this specific user, at this
 * permission level". The file's owner is available via file.getOwner() -
 * no need to duplicate that here.
 */
@Entity
@Table(name = "file_shares",
        uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "shared_with_user_id"}),
        indexes = {
                @Index(name = "idx_shares_file", columnList = "file_id"),
                @Index(name = "idx_shares_shared_with", columnList = "shared_with_user_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileItem file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
