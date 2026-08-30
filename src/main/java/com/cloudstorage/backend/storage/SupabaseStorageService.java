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

@Service
public class SupabaseStorageService implements StorageService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${supabase.url}")
    private String supabaseUrl;

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

        // Delete is deliberately idempotent: if Supabase says the object
        // is already gone (404), that's fine - the caller's actual goal
        // ("this file shouldn't exist in storage") is already true. Only
        // treat genuine failures (bad credentials, network issues, etc.)
        // as errors worth surfacing.
        sendAllowingNotFound(request, "delete");
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

    private void sendAllowingNotFound(HttpRequest request, String action) throws IOException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Supabase Storage doesn't actually return a real HTTP 404 for
            // "object not found" on this endpoint - it returns HTTP 400
            // with the true error described INSIDE the JSON body instead
            // (e.g. {"statusCode":"404","code":"NoSuchKey",...}). So we
            // check the body content for that signal, not just the status
            // code - confirmed by hitting this exact case in testing.
            boolean isAlreadyGone = response.body() != null
                    && (response.body().contains("NoSuchKey") || response.body().contains("\"statusCode\":\"404\""));

            if (response.statusCode() >= 400 && !isAlreadyGone) {
                throw new IOException("Supabase storage " + action + " failed ("
                        + response.statusCode() + "): " + response.body());
            }
            // Either success, or the object was already gone - both are
            // fine for a delete, since the caller's goal is already true.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase storage " + action + " was interrupted", e);
        }
    }

    private String encodePath(String path) {
        String[] segments = path.split("/");
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) encoded.append("/");
            encoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return encoded.toString();
    }
}