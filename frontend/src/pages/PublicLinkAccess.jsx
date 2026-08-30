import { useState, useEffect, useCallback } from "react";
import { useParams } from "react-router-dom";
import { Download, Lock, AlertCircle, Loader2 } from "lucide-react";
import { accessPublicLink } from "../api/publicLinks";
import { formatFileSize } from "../utils/format";

// Unauthenticated page - reachable by anyone with the link, no login.
// The backend's access endpoint is a POST (password goes in the body,
// not the URL), so this page exists specifically to make that into a
// normal clickable link: it does the POST on the visitor's behalf.
export default function PublicLinkAccess() {
  const { token } = useParams();

  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [needsPassword, setNeedsPassword] = useState(false);
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const attemptAccess = useCallback(async (pwd) => {
    setError("");
    try {
      const data = await accessPublicLink(token, pwd);
      setFile(data);
      setNeedsPassword(false);
    } catch (err) {
      const message = err.response?.data?.error || "This link is invalid or has expired.";
      if (message === "Incorrect password") {
        setNeedsPassword(true);
      } else {
        setError(message);
      }
    }
  }, [token]);

  useEffect(() => {
    attemptAccess(null).finally(() => setLoading(false));
  }, [attemptAccess]);

  async function handlePasswordSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    await attemptAccess(password);
    setSubmitting(false);
  }

  return (
    <div className="min-h-screen bg-[#0f1115] flex items-center justify-center px-4">
      <div className="w-full max-w-sm text-center">
        {loading && <Loader2 size={28} className="text-gray-600 animate-spin mx-auto" />}

        {!loading && error && (
          <div className="flex flex-col items-center gap-3">
            <AlertCircle size={32} className="text-red-400" />
            <p className="text-sm text-gray-400">{error}</p>
          </div>
        )}

        {!loading && needsPassword && !file && (
          <div className="bg-[#16181f] border border-white/5 rounded-2xl p-6">
            <Lock size={24} className="text-indigo-400 mx-auto mb-3" />
            <p className="text-sm text-gray-300 mb-4">This file is password protected.</p>
            <form onSubmit={handlePasswordSubmit}>
              <input
                type="password"
                autoFocus
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                className="w-full mb-3 px-3 py-2.5 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-sm
                           placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50"
              />
              {error === "" && password && submitting === false && null}
              <button
                type="submit"
                disabled={submitting}
                className="w-full py-2.5 rounded-lg bg-indigo-500 hover:bg-indigo-400 disabled:opacity-50
                           text-white text-sm font-medium transition-colors"
              >
                {submitting ? "Checking..." : "Unlock"}
              </button>
            </form>
          </div>
        )}

        {!loading && file && (
          <div className="bg-[#16181f] border border-white/5 rounded-2xl p-6">
            <p className="text-sm text-gray-100 font-medium mb-1 truncate" title={file.name}>
              {file.name}
            </p>
            <p className="text-xs text-gray-600 mb-5">{formatFileSize(file.size)}</p>
            <a
              href={file.downloadUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg bg-indigo-500 hover:bg-indigo-400
                         text-white text-sm font-medium transition-colors"
            >
              <Download size={16} />
              Download
            </a>
          </div>
        )}
      </div>
    </div>
  );
}
