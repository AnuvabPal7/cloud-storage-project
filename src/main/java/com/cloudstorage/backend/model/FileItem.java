package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Named FileItem, not File, to avoid clashing with java.io.File - an easy
 * mistake to make and a confusing one to debug if the wrong import sneaks in.
 */
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder; // null = root level, sits directly in "My Drive"

    @Column(nullable = false)
    private String name; // original filename shown to the user

    // Where this file actually lives in the storage bucket - never the same
    // as `name`, since we prefix it with owner+id to avoid collisions and
    // keep users' files isolated from each other in the bucket.
    @Column(name = "storage_path", nullable = false, unique = true)
    private String storagePath;

    @Column(nullable = false)
    private Long size; // bytes

    @Column(name = "mime_type")
    private String mimeType;

    // Soft-delete flag for Trash, built out properly on Day 4. Defaults to
    // false so today's uploads behave normally.
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
