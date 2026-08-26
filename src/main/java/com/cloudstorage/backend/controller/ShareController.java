package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.CreateShareRequest;
import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.ShareResponse;
import com.cloudstorage.backend.dto.SharedFileResponse;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.FileService;
import com.cloudstorage.backend.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;
    private final FileService fileService;

    // Creates a new share, or updates the permission if one already exists
    // for that file + person - see ShareService for why.
    @PostMapping
    public ResponseEntity<ShareResponse> create(@Valid @RequestBody CreateShareRequest request,
                                                 @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(shareService.createOrUpdateShare(request, owner));
    }

    // Owner-only: who is this file currently shared with?
    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<ShareResponse>> listForFile(@PathVariable UUID fileId,
                                                             @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(shareService.listSharesForFile(fileId, owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        shareService.revokeShare(id, owner);
        return ResponseEntity.noContent().build();
    }

    // Files that OTHER people have shared with the current user.
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedFileResponse>> sharedWithMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(shareService.listSharedWithMe(user));
    }

    // The actual permission-checked download path for a Viewer/Editor -
    // separate from the owner-only GET /api/files/{id}.
    @GetMapping("/download/{fileId}")
    public ResponseEntity<FileResponse> download(@PathVariable UUID fileId,
                                                  @AuthenticationPrincipal User user) throws IOException {
        return ResponseEntity.ok(fileService.getSharedDownloadUrl(fileId, user));
    }
}
