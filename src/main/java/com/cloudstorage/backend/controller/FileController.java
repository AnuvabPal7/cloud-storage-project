package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.service.FileService;
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

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.upload(file, owner));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> listFiles(@AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(fileService.listRootFiles(owner));
    }

    // Returns metadata + a time-limited signed download URL (not the raw
    // file bytes) - the browser downloads directly from storage using it.
    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable UUID id,
                                                 @AuthenticationPrincipal User owner) throws IOException {
        return ResponseEntity.ok(fileService.getWithDownloadUrl(id, owner));
    }
}
