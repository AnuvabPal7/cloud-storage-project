package com.cloudstorage.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record PublicLinkResponse(
        UUID id,
        String token,
        Instant expiresAt,
        boolean hasPassword,
        Instant createdAt
) {}
