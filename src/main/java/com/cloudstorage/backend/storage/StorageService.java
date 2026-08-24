package com.cloudstorage.backend.storage;

import java.io.IOException;

/**
 * Everything above this interface (FileService, controllers) only ever
 * talks to StorageService - never to Supabase directly. If you swap to
 * AWS S3 later, you write one new class implementing this interface and
 * change zero lines anywhere else in the app. That's the whole point of
 * coding to an interface instead of a concrete class.
 */
public interface StorageService {

    void upload(String path, byte[] content, String contentType) throws IOException;

    void delete(String path) throws IOException;

    /**
     * Returns a temporary, signed URL the browser can use to download the
     * file directly from storage - without ever exposing your bucket
     * publicly or routing the file bytes through your own server.
     */
    String createSignedUrl(String path, int expiresInSeconds) throws IOException;
}
