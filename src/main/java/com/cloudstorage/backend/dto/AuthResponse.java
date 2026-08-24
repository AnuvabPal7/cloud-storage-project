package com.cloudstorage.backend.dto;

public record AuthResponse(
        String token,
        String email,
        String name
) {}
