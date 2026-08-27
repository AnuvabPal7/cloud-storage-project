import api from "./client";

export async function login(email, password) {
  const response = await api.post("/api/auth/login", { email, password });
  return response.data; // { token, email, name }
}

export async function register(name, email, password) {
  const response = await api.post("/api/auth/register", { name, email, password });
  return response.data; // { token, email, name }
}

export async function fetchMe() {
  const response = await api.get("/api/auth/me");
  return response.data; // { email, name }
}
