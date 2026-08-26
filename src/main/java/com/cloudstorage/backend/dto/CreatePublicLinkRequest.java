package com.cloudstorage.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePublicLinkRequest(

        @NotNull(message = "fileId is required")
        UUID fileId,

        // Optional - defaults to 168 hours (7 days) if omitted.
        Integer expiresInHours,

        // Optional - link has no password if omitted/blank.
        String password
) {}
