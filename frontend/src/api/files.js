import api from "./client";

// The 50MB matches Supabase Storage's free-tier hard limit (confirmed in
// their dashboard) - checking client-side means a too-large file gets an
// instant, clear rejection instead of uploading partway then failing.
export const MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;

export async function uploadFile(file, folderId, onProgress) {
  const formData = new FormData();
  formData.append("file", file);
  const params = folderId ? { folderId } : {};

  const response = await api.post("/api/files/upload", formData, {
    params,
    onUploadProgress: (event) => {
      if (onProgress && event.total) {
        onProgress(Math.round((event.loaded * 100) / event.total));
      }
    },
  });
  return response.data;
}

// Returns file metadata + a signed downloadUrl.
export async function getFile(fileId) {
  const response = await api.get(`/api/files/${fileId}`);
  return response.data;
}

export async function renameFile(fileId, name) {
  const response = await api.patch(`/api/files/${fileId}`, { name });
  return response.data;
}

export async function moveFile(fileId, folderId) {
  const response = await api.patch(`/api/files/${fileId}/move`, { folderId });
  return response.data;
}

export async function softDeleteFile(fileId) {
  await api.delete(`/api/files/${fileId}`);
}

export async function restoreFile(fileId) {
  const response = await api.post(`/api/files/${fileId}/restore`);
  return response.data;
}

export async function listTrash() {
  const response = await api.get("/api/files/trash");
  return response.data;
}

export async function permanentlyDeleteFile(fileId) {
  await api.delete(`/api/files/${fileId}/permanent`);
}

// Returns a Page<FileResponse> - { content, totalPages, number, ... }
export async function searchFiles({ query, mimeType, page = 0, size = 20, sortBy = "createdAt", sortDir = "desc" }) {
  const response = await api.get("/api/files/search", {
    params: { query, mimeType, page, size, sortBy, sortDir },
  });
  return response.data;
}

// Returns { usedBytes, limitBytes } - the limit is a soft, per-user quota
// the backend defines for display purposes (matches Supabase's free-tier
// 1GB), not something Supabase itself enforces per user.
export async function getStorageUsage() {
  const response = await api.get("/api/files/storage-usage");
  return response.data;
}
