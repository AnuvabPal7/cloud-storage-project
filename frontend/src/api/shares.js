import api from "./client";

export async function createShare(fileId, email, permission) {
  const response = await api.post("/api/shares", { fileId, email, permission });
  return response.data;
}

export async function listSharesForFile(fileId) {
  const response = await api.get(`/api/shares/file/${fileId}`);
  return response.data;
}

export async function revokeShare(shareId) {
  await api.delete(`/api/shares/${shareId}`);
}

export async function listSharedWithMe() {
  const response = await api.get("/api/shares/shared-with-me");
  return response.data;
}

// Permission-checked download for a file shared with the current user
// (as opposed to api/files.js's getFile, which is owner-only).
export async function getSharedDownloadUrl(fileId) {
  const response = await api.get(`/api/shares/download/${fileId}`);
  return response.data;
}
