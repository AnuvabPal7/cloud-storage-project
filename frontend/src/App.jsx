import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import PublicLinkAccess from "./pages/PublicLinkAccess";

export default function App() {
  const { user, loading } = useAuth();

  return (
    <Routes>
      {/* If you're already logged in, /login and /signup just bounce you
          straight to the dashboard instead of showing the form again. */}
      <Route
        path="/login"
        element={!loading && user ? <Navigate to="/dashboard" replace /> : <Login />}
      />
      <Route
        path="/signup"
        element={!loading && user ? <Navigate to="/dashboard" replace /> : <Signup />}
      />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      {/* Public - no login required, reachable by anyone with the link */}
      <Route path="/share/:token" element={<PublicLinkAccess />} />

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
