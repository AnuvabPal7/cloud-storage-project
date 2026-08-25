package com.cloudstorage.backend.dto;

import java.util.List;

// What the frontend needs to render one "screen" of the file explorer:
// which folder you're looking at (null = root/"My Drive"), plus its
// immediate subfolders and files. Not the whole tree - just one level,
// same as how Google Drive loads a folder as you click into it.
public record FolderContentsResponse(
        FolderResponse folder,
        List<FolderResponse> subfolders,
        List<FileResponse> files
) {}
