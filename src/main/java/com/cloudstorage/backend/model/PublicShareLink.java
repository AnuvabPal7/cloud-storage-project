package com.cloudstorage.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * A public link: anyone with the token can access the file (subject to
 * expiry and an optional password), no login required. `token` is what
 * goes in the shareable URL - it's deliberately separate from the row's
 * own id, so guessing/incrementing a UUID id can't be used to find links
 * (defense in depth, on top of the token already being long and random).
 */
@Entity
@Table(name = "public_share_links",
        indexes = @Index(name = "idx_public_links_token", columnList = "token", unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileItem file;

    @Column(nullable = false, unique = true)
    private String token;

    // BCrypt hash, or null if the link has no password.
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
