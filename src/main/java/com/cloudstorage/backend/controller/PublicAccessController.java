package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.dto.FileResponse;
import com.cloudstorage.backend.dto.PublicLinkAccessRequest;
import com.cloudstorage.backend.service.PublicLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * The one and only unauthenticated file-access path in the whole app -
 * this is what SecurityConfig's "/api/public/**" permitAll rule exists
 * for. No @AuthenticationPrincipal anywhere in this class, on purpose.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicAccessController {

    private final PublicLinkService publicLinkService;

    // POST (not GET) so a password goes in the JSON body, not the URL -
    // URLs end up in server logs and browser history; request bodies don't.
    @PostMapping("/files/{token}/access")
    public ResponseEntity<FileResponse> access(@PathVariable String token,
                                                @RequestBody(required = false) PublicLinkAccessRequest request)
            throws IOException {
        String password = (request != null) ? request.password() : null;
        return ResponseEntity.ok(publicLinkService.access(token, password));
    }
}
