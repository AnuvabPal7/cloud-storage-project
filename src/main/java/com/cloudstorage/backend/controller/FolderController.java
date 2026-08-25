package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.CreateFolderRequest;
import com.cloudstorage.backend.dto.FolderContentsResponse;
import com.cloudstorage.backend.dto.FolderResponse;
import com.cloudstorage.backend.dto.RenameRequest;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest request,
                                                  @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(folderService.create(request, owner));
    }

    // No folderId query param -> root level ("My Drive" top screen).
    // ?folderId=<uuid> -> that folder's immediate contents.
    @GetMapping
    public ResponseEntity<FolderContentsResponse> getContents(
            @RequestParam(required = false) UUID folderId,
            @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(folderService.getContents(folderId, owner));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FolderResponse> rename(@PathVariable UUID id,
                                                  @Valid @RequestBody RenameRequest request,
                                                  @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(folderService.rename(id, request.name(), owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        folderService.delete(id, owner);
        return ResponseEntity.noContent().build();
    }
}
