package com.cloudstorage.backend.dto;

import com.cloudstorage.backend.model.SharePermission;

import java.time.Instant;
import java.util.UUID;

public record ShareResponse(
        UUID id,
        UUID fileId,
        String fileName,
        String sharedWithEmail,
        SharePermission permission,
        Instant createdAt
) {}
