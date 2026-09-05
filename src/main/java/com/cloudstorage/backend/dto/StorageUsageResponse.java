package com.cloudstorage.backend.dto;

// limitBytes is a soft, per-user quota WE define for the UI (matches
// Supabase's free-tier 1GB) - not something Supabase actually enforces
// per user. Supabase's real 1GB limit is shared across the whole bucket,
// across every user of the app. Showing a per-user number here is a
// simplification that makes sense for a demo/portfolio app with a
// handful of users, worth knowing the difference if this ever needed to
// scale to real multi-tenant quotas.
public record StorageUsageResponse(
        long usedBytes,
        long limitBytes
) {}
