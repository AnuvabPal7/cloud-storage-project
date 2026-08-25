package com.cloudstorage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateFolderRequest(

        @NotBlank(message = "Folder name is required")
        @Size(max = 255, message = "Folder name is too long")
        String name,

        UUID parentId // null = create at root level ("My Drive")
) {}
