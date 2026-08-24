package com.cloudstorage.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String name,
        long size,
        String mimeType,
        Instant createdAt,
        String downloadUrl // null on list/upload responses; populated on GET /{id}
) {}
