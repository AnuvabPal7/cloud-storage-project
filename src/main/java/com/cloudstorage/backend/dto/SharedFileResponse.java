package com.cloudstorage.backend.dto;

import com.cloudstorage.backend.model.SharePermission;

import java.time.Instant;
import java.util.UUID;

// What shows up in "Shared with me" - a file plus the permission level
// the current user has on it and who originally owns it.
public record SharedFileResponse(
        UUID fileId,
        String name,
        long size,
        String mimeType,
        SharePermission permission,
        String ownerEmail,
        Instant createdAt
) {}
