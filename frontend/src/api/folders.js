import api from "./client";

// folderId = null -> root level ("My Drive" top screen).
// Returns { folder, subfolders: [...], files: [...] }
export async function getFolderContents(folderId) {
  const params = folderId ? { folderId } : {};
  const response = await api.get("/api/folders", { params });
  return response.data;
}

export async function createFolder(name, parentId) {
  const response = await api.post("/api/folders", { name, parentId });
  return response.data;
}

export async function renameFolder(folderId, name) {
  const response = await api.patch(`/api/folders/${folderId}`, { name });
  return response.data;
}

export async function deleteFolder(folderId) {
  await api.delete(`/api/folders/${folderId}`);
}
