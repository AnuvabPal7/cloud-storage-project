package com.cloudstorage.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Folders are a plain list (unpaginated - see FolderRepository.search for
// why). Files stay paginated since file counts can get much larger.
// mimeType filtering only applies to files - folders have no mime type,
// so they're matched purely by name regardless of that filter.
public record SearchResponse(
        List<FolderResponse> folders,
        Page<FileResponse> files
) {}
