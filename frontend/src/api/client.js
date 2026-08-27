import axios from "axios";

// VITE_API_BASE_URL comes from .env (local: http://localhost:8080).
// Once the backend is deployed and this frontend is deployed too, set
// this to your Render URL in whatever hosting platform's env var UI -
// never hardcode it, so the same code works in both places.
const baseURL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({ baseURL });

// Attaches the JWT to every outgoing request automatically, so individual
// pages never have to remember to add the Authorization header themselves.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the backend ever says "your token isn't valid" (401/403), the
// simplest correct response is to log the user out and send them back
// to login - there's no token-refresh flow in this app, so there's
// nothing else meaningful to do with an expired/invalid token.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
