package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.MoveFileRequest;
import com.cloudstorage.backend.dto.RenameRequest;
import com.cloudstorage.backend.dto.SearchResponse;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // folderId is optional - omit it to upload to the root ("My Drive").
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file,
                                                @RequestParam(required = false) UUID folderId,
                                                @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.upload(file, folderId, owner));
    }

    // Root-level files only. Files inside a specific folder are returned
    // by GET /api/folders?folderId={id} instead (see FolderController) -
    // keeps "what's in this folder" logic in one place rather than two.
    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(@AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.listRootFiles(owner));
    }

    // Global search across everything in Drive - files AND folders, same
    // idea as the Google Drive search bar. All params are optional.
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String mimeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.search(query, mimeType, page, size, sortBy, sortDir, owner));
    }

    // Returns metadata + a time-limited signed download URL (not the raw
    // file bytes) - the browser downloads directly from storage using it.
    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable UUID id,
                                                 @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.getWithDownloadUrl(id, owner));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileResponse> rename(@PathVariable UUID id,
                                                @Valid @RequestBody RenameRequest request,
                                                @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.rename(id, request.name(), owner));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<FileResponse> move(@PathVariable UUID id,
                                              @RequestBody MoveFileRequest request,
                                              @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.move(id, request.folderId(), owner));
    }

    // Soft delete - sends the file to Trash. Nothing is destroyed yet.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        fileService.softDelete(id, owner);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<FileResponse> restore(@PathVariable UUID id, @AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.restore(id, owner));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<FileResponse>> listTrash(@AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.listTrash(owner));
    }

    // Only works on a file that's already in Trash - a deliberate two-step
    // safety net against destroying something with a single misclick.
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDelete(@PathVariable UUID id,
                                                   @AuthenticationPrincipal User owner) throws IOException {
        fileService.permanentlyDelete(id, owner);
        return ResponseEntity.noContent().build();
    }
}
