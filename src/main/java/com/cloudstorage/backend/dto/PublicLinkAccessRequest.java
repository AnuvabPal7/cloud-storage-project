package com.cloudstorage.backend.dto;

// Sent as a JSON body (not a query param) specifically so a password
// doesn't end up sitting in plain text in server access logs or browser
// history the way a query string would.
public record PublicLinkAccessRequest(
        String password // null/omitted if the link has no password
) {}
