package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * File organization - built out fully on Day 4 (create, rename, delete,
 * nested hierarchy). A null folder on a File means "lives at the root"
 * (My Drive top level).
 */
@Entity
@Table(name = "folders", indexes = {
        @Index(name = "idx_folders_owner", columnList = "owner_id"),
        // Speeds up "list this folder's subfolders" - what getContents() runs.
        @Index(name = "idx_folders_owner_parent", columnList = "owner_id, parent_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent; // null = top-level folder

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
