import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      // The backend's GlobalExceptionHandler returns { error: "..." } for
      // auth failures - fall back to a generic message if the shape ever
      // differs (e.g. a network error with no response body at all).
      setError(err.response?.data?.error || "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#0f1115] flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 mb-4">
            <svg viewBox="0 0 24 24" fill="none" className="w-6 h-6 text-indigo-400">
              <path
                d="M6 19a4 4 0 01-.5-7.97A5.5 5.5 0 0116.9 8.05 4.5 4.5 0 0118 17H6z"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          <h1 className="text-xl font-semibold text-gray-100">Welcome back</h1>
          <p className="text-sm text-gray-500 mt-1">Sign in to your Drive</p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-[#16181f] border border-white/5 rounded-2xl p-6 shadow-xl shadow-black/20"
        >
          {error && (
            <div className="mb-4 px-3 py-2.5 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-sm">
              {error}
            </div>
          )}

          <label className="block text-xs font-medium text-gray-400 mb-1.5">Email</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full mb-4 px-3 py-2.5 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-sm
                       placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50
                       transition-colors"
            placeholder="you@example.com"
          />

          <label className="block text-xs font-medium text-gray-400 mb-1.5">Password</label>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full mb-6 px-3 py-2.5 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-sm
                       placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50
                       transition-colors"
            placeholder="••••••••"
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 rounded-lg bg-indigo-500 hover:bg-indigo-400 disabled:opacity-50
                       disabled:cursor-not-allowed text-white text-sm font-medium transition-colors"
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{" "}
          <Link to="/signup" className="text-indigo-400 hover:text-indigo-300 font-medium">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  );
}
