import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Signup() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(name, email, password);
      navigate("/dashboard");
    } catch (err) {
      // Field-level validation errors come back as { name: "...", email: "..." }
      // rather than { error: "..." } - grab whichever field's message exists.
      const data = err.response?.data;
      const message =
        data?.error || data?.name || data?.email || data?.password || "Something went wrong. Please try again.";
      setError(message);
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
          <h1 className="text-xl font-semibold text-gray-100">Create your account</h1>
          <p className="text-sm text-gray-500 mt-1">Start storing your files</p>
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

          <label className="block text-xs font-medium text-gray-400 mb-1.5">Full name</label>
          <input
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full mb-4 px-3 py-2.5 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-sm
                       placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50
                       transition-colors"
            placeholder="Letters and spaces only"
          />

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
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full mb-1.5 px-3 py-2.5 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-sm
                       placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50
                       transition-colors"
            placeholder="At least 8 characters"
          />
          <p className="text-xs text-gray-600 mb-5">Minimum 8 characters</p>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 rounded-lg bg-indigo-500 hover:bg-indigo-400 disabled:opacity-50
                       disabled:cursor-not-allowed text-white text-sm font-medium transition-colors"
          >
            {loading ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Already have an account?{" "}
          <Link to="/login" className="text-indigo-400 hover:text-indigo-300 font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
