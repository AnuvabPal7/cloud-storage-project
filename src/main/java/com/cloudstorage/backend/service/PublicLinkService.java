package com.cloudstorage.backend.service;

import com.cloudstorage.backend.dto.CreatePublicLinkRequest;
import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.PublicLinkResponse;
import com.cloudstorage.backend.model.FileItem;
import com.cloudstorage.backend.model.PublicShareLink;
import com.cloudstorage.backend.model.User;
import com.cloudstorage.backend.repository.FileRepository;
import com.cloudstorage.backend.repository.PublicShareLinkRepository;
import com.cloudstorage.backend.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicLinkService {

    private static final int DEFAULT_EXPIRY_HOURS = 168; // 7 days
    private static final int DOWNLOAD_URL_EXPIRY_SECONDS = 3600;

    private final FileRepository fileRepository;
    private final PublicShareLinkRepository publicShareLinkRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder; // reusing the same BCrypt bean from SecurityConfig

    @Transactional
    public PublicLinkResponse create(CreatePublicLinkRequest request, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(request.fileId(), owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        int hours = (request.expiresInHours() != null && request.expiresInHours() > 0)
                ? request.expiresInHours()
                : DEFAULT_EXPIRY_HOURS;

        String passwordHash = (request.password() != null && !request.password().isBlank())
                ? passwordEncoder.encode(request.password())
                : null;

        PublicShareLink link = PublicShareLink.builder()
                .file(file)
                .token(generateToken())
                .passwordHash(passwordHash)
                .expiresAt(Instant.now().plus(hours, ChronoUnit.HOURS))
                .build();

        publicShareLinkRepository.save(link);
        return toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<PublicLinkResponse> listForFile(UUID fileId, User owner) {
        FileItem file = fileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        return publicShareLinkRepository.findAllByFile(file).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void revoke(UUID linkId, User owner) {
        PublicShareLink link = publicShareLinkRepository.findByIdAndFile_Owner(linkId, owner)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        publicShareLinkRepository.delete(link);
    }

    /**
     * The actual public access path - no @AuthenticationPrincipal, no login
     * needed. Anyone with the token (and password, if the link has one)
     * gets a signed download URL. Deliberately uses the same error message
     * for "link doesn't exist", "link expired", so an attacker can't use
     * the message to distinguish a dead token from a live one.
     */
    @Transactional(readOnly = true)
    public FileResponse access(String token, String providedPassword) throws IOException {
        PublicShareLink link = publicShareLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("This link is invalid or has expired"));

        if (link.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("This link is invalid or has expired");
        }

        if (link.getPasswordHash() != null) {
            if (providedPassword == null || !passwordEncoder.matches(providedPassword, link.getPasswordHash())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        FileItem file = link.getFile();
        String signedUrl = storageService.createSignedUrl(file.getStoragePath(), DOWNLOAD_URL_EXPIRY_SECONDS);

        return new FileResponse(file.getId(), file.getName(), file.getSize(),
                file.getMimeType(), file.getCreatedAt(), signedUrl);
    }

    private String generateToken() {
        // Two concatenated UUIDs (no dashes) = 64 hex chars, well beyond
        // what's practically guessable - plenty of entropy for a share link.
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private PublicLinkResponse toResponse(PublicShareLink link) {
        return new PublicLinkResponse(
                link.getId(),
                link.getToken(),
                link.getExpiresAt(),
                link.getPasswordHash() != null,
                link.getCreatedAt()
        );
    }
}
