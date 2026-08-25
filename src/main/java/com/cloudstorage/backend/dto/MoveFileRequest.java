package com.cloudstorage.backend.dto;

import java.util.UUID;

public record MoveFileRequest(
        UUID folderId // null = move to root level
) {}
