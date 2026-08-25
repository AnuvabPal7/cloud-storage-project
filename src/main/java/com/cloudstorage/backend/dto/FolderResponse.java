package com.cloudstorage.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record FolderResponse(
        UUID id,
        String name,
        UUID parentId, // null if this is a root-level folder
        Instant createdAt
) {}
