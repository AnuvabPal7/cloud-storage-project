package com.cloudstorage.backend.dto;

import com.cloudstorage.backend.model.SharePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateShareRequest(

        @NotNull(message = "fileId is required")
        UUID fileId,

        // Sharing by email (not a user id) is more natural for a real UI -
        // the owner types in who they want to share with, same as Google Drive.
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotNull(message = "Permission is required")
        SharePermission permission
) {}
