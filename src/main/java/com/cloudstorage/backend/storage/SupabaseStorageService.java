package com.cloudstorage.backend.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Talks to Supabase's Storage REST API directly over HTTP. There's no
 * official Supabase Java SDK, so we use Java's built-in HttpClient
 * (java.net.http, no extra dependency needed) instead of pulling in a
 * whole new library just for a few HTTP calls.
 *
 * Docs: https://supabase.com/docs/guides/storage
 */
@Service
public class SupabaseStorageService implements StorageService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${supabase.url}")
    private String supabaseUrl;

    // The SERVICE ROLE key (not the anon/public key!) is required for the
    // backend to upload on a user's behalf, bypassing bucket-level RLS.
    // Never send this key to the frontend - it has full admin access.
    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Override
    public void upload(String path, byte[] content, String contentType) throws IOException {
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(path);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                // upsert=true lets a re-upload to the same path overwrite cleanly
                // instead of erroring - handy while we're not building
                // versioning yet (that's a Phase 2 feature per the spec).
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();

        send(request, "upload");
    }

    @Override
    public void delete(String path) throws IOException {
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(path);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .DELETE()
                .build();

        send(request, "delete");
    }

    @Override
    public String createSignedUrl(String path, int expiresInSeconds) throws IOException {
        String url = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + encodePath(path);

        String body = objectMapper.writeValueAsString(Map.of("expiresIn", expiresInSeconds));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = send(request, "create signed URL");

        // Supabase returns {"signedURL": "/object/sign/bucket/path?token=..."} -
        // a relative path, so we prefix it with the storage API base ourselves.
        JsonNode json = objectMapper.readTree(response.body());
        String signedPath = json.get("signedURL").asText();
        return supabaseUrl + "/storage/v1" + signedPath;
    }

    private HttpResponse<String> send(HttpRequest request, String action) throws IOException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IOException("Supabase storage " + action + " failed ("
                        + response.statusCode() + "): " + response.body());
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase storage " + action + " was interrupted", e);
        }
    }

    private String encodePath(String path) {
        // Encode each segment separately so the "/" separators in the path
        // survive - encoding the whole path at once would turn them into %2F.
        String[] segments = path.split("/");
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) encoded.append("/");
            encoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return encoded.toString();
    }
}
