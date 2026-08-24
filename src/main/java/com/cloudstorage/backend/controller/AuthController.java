package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.AuthResponse;
import com.cloudstorage.backend.dto.LoginRequest;
import com.cloudstorage.backend.dto.RegisterRequest;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // @AuthenticationPrincipal pulls the User straight out of the JWT-authenticated
    // request - proof the whole chain (filter -> SecurityContext -> here) works.
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new AuthResponse(null, user.getEmail(), user.getName()));
    }
}
