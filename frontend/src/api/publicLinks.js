import api from "./client";

export async function createPublicLink(fileId, expiresInHours, password) {
  const response = await api.post("/api/public-links", { fileId, expiresInHours, password });
  return response.data;
}

export async function listLinksForFile(fileId) {
  const response = await api.get(`/api/public-links/file/${fileId}`);
  return response.data;
}

export async function revokePublicLink(linkId) {
  await api.delete(`/api/public-links/${linkId}`);
}

// The actual public access call - no auth needed (backend permits this
// route without a token). Password goes in the JSON body deliberately -
// a raw GET URL can't carry a password safely (server logs, browser
// history), so this always goes through a POST.
export async function accessPublicLink(token, password) {
  const response = await api.post(`/api/public/files/${token}/access`, { password: password || null });
  return response.data;
}
