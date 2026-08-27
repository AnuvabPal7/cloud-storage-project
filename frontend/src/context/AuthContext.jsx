import { createContext, useContext, useState, useEffect } from "react";
import * as authApi from "../api/auth";

const AuthContext = createContext(null);

// Wrap the whole app in this once (see main.jsx). Any component can then
// call useAuth() to read the current user or trigger login/logout -
// no need to pass user/token down through props everywhere.
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On first load, if a token is already sitting in localStorage from a
  // previous session, restore the user's session instead of forcing them
  // to log in again every time they refresh the page.
  useEffect(() => {
    const token = localStorage.getItem("token");
    const savedUser = localStorage.getItem("user");
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  function saveSession(data) {
    // data = { token, email, name } - the exact shape AuthResponse returns
    const userData = { email: data.email, name: data.name };
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);
  }

  async function login(email, password) {
    const data = await authApi.login(email, password);
    saveSession(data);
  }

  async function register(name, email, password) {
    const data = await authApi.register(name, email, password);
    saveSession(data);
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
