package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.CreatePublicLinkRequest;
import com.cloudstorage.backend.dto.PublicLinkResponse;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.PublicLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Everything here requires login - it's for the OWNER to create/manage
// links. Actually USING a link (no login) is PublicAccessController.
@RestController
@RequestMapping("/api/public-links")
@RequiredArgsConstructor
public class PublicLinkController {

    private final PublicLinkService publicLinkService;

    @PostMapping
    public ResponseEntity<PublicLinkResponse> create(@Valid @RequestBody CreatePublicLinkRequest request,
                                                       @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(publicLinkService.create(request, owner));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<PublicLinkResponse>> listForFile(@PathVariable UUID fileId,
                                                                  @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(publicLinkService.listForFile(fileId, owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        publicLinkService.revoke(id, owner);
        return ResponseEntity.noContent().build();
    }
}
