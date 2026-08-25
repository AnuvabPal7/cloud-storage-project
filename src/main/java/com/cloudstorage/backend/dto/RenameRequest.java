package com.cloudstorage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Shared by both file rename and folder rename - same shape, same rules.
public record RenameRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name is too long")
        String name
) {}
