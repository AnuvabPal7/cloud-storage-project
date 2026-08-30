import { useState, useEffect, useCallback } from "react";
import { X, Link2, Copy, Check, Trash2, UserPlus } from "lucide-react";
import * as sharesApi from "../api/shares";
import * as linksApi from "../api/publicLinks";

const PERMISSIONS = ["VIEWER", "EDITOR"];

export default function ShareModal({ file, onClose }) {
  const [shares, setShares] = useState([]);
  const [links, setLinks] = useState([]);
  const [loading, setLoading] = useState(true);

  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState("VIEWER");
  const [shareError, setShareError] = useState("");
  const [sharing, setSharing] = useState(false);

  const [linkError, setLinkError] = useState("");
  const [creatingLink, setCreatingLink] = useState(false);
  const [copiedId, setCopiedId] = useState(null);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [shareList, linkList] = await Promise.all([
        sharesApi.listSharesForFile(file.id),
        linksApi.listLinksForFile(file.id),
      ]);
      setShares(shareList);
      setLinks(linkList);
    } catch {
      // Non-fatal - the modal still works for creating new shares/links
      // even if the initial list fetch has a hiccup.
    } finally {
      setLoading(false);
    }
  }, [file.id]);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  async function handleAddShare(e) {
    e.preventDefault();
    setShareError("");
    setSharing(true);
    try {
      await sharesApi.createShare(file.id, email.trim(), permission);
      setEmail("");
      await loadAll();
    } catch (err) {
      setShareError(err.response?.data?.error || "Couldn't share the file.");
    } finally {
      setSharing(false);
    }
  }

  async function handleRevokeShare(shareId) {
    await sharesApi.revokeShare(shareId);
    setShares((prev) => prev.filter((s) => s.id !== shareId));
  }

  async function handleCreateLink() {
    setLinkError("");
    setCreatingLink(true);
    try {
      await linksApi.createPublicLink(file.id, 168, null); // 7 days, no password
      await loadAll();
    } catch (err) {
      setLinkError(err.response?.data?.error || "Couldn't create the link.");
    } finally {
      setCreatingLink(false);
    }
  }

  async function handleRevokeLink(linkId) {
    await linksApi.revokePublicLink(linkId);
    setLinks((prev) => prev.filter((l) => l.id !== linkId));
  }

  function copyLink(link) {
    const url = `${window.location.origin}/share/${link.token}`;
    navigator.clipboard.writeText(url);
    setCopiedId(link.id);
    setTimeout(() => setCopiedId(null), 1500);
  }

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 px-4" onClick={onClose}>
      <div
        className="w-full max-w-md bg-[#16181f] border border-white/10 rounded-2xl shadow-2xl max-h-[85vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-white/5 shrink-0">
          <div className="min-w-0">
            <h2 className="text-sm font-semibold text-gray-100">Share</h2>
            <p className="text-xs text-gray-500 truncate" title={file.name}>
              {file.name}
            </p>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-300 shrink-0">
            <X size={18} />
          </button>
        </div>

        <div className="overflow-y-auto px-5 py-4 space-y-6">
          {/* People with access */}
          <section>
            <h3 className="text-xs font-medium text-gray-400 mb-2.5">People with access</h3>

            <form onSubmit={handleAddShare} className="flex gap-2 mb-3">
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email address"
                className="flex-1 min-w-0 px-3 py-2 rounded-lg bg-[#0f1115] border border-white/10 text-gray-100 text-xs
                           placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50"
              />
              <select
                value={permission}
                onChange={(e) => setPermission(e.target.value)}
                className="px-2 py-2 rounded-lg bg-[#0f1115] border border-white/10 text-gray-300 text-xs
                           focus:outline-none focus:border-indigo-500/50"
              >
                {PERMISSIONS.map((p) => (
                  <option key={p} value={p}>
                    {p === "VIEWER" ? "Viewer" : "Editor"}
                  </option>
                ))}
              </select>
              <button
                type="submit"
                disabled={sharing}
                className="p-2 rounded-lg bg-indigo-500 hover:bg-indigo-400 disabled:opacity-50 text-white shrink-0 transition-colors"
                title="Share"
              >
                <UserPlus size={14} />
              </button>
            </form>

            {shareError && <p className="text-xs text-red-400 mb-2">{shareError}</p>}

            {loading ? (
              <p className="text-xs text-gray-600">Loading...</p>
            ) : shares.length === 0 ? (
              <p className="text-xs text-gray-600">Not shared with anyone yet.</p>
            ) : (
              <ul className="space-y-1.5">
                {shares.map((s) => (
                  <li
                    key={s.id}
                    className="flex items-center justify-between gap-2 px-3 py-2 rounded-lg bg-[#0f1115] border border-white/5"
                  >
                    <span className="text-xs text-gray-300 truncate">{s.sharedWithEmail}</span>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="text-[10px] uppercase tracking-wide text-gray-500">{s.permission}</span>
                      <button
                        onClick={() => handleRevokeShare(s.id)}
                        className="text-gray-600 hover:text-red-400 transition-colors"
                        title="Remove access"
                      >
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* Public links */}
          <section>
            <div className="flex items-center justify-between mb-2.5">
              <h3 className="text-xs font-medium text-gray-400">Public links</h3>
              <button
                onClick={handleCreateLink}
                disabled={creatingLink}
                className="flex items-center gap-1 text-xs text-indigo-400 hover:text-indigo-300 disabled:opacity-50 transition-colors"
              >
                <Link2 size={12} />
                {creatingLink ? "Creating..." : "New link"}
              </button>
            </div>

            {linkError && <p className="text-xs text-red-400 mb-2">{linkError}</p>}

            {loading ? (
              <p className="text-xs text-gray-600">Loading...</p>
            ) : links.length === 0 ? (
              <p className="text-xs text-gray-600">No public links yet. Anyone with a link can view the file.</p>
            ) : (
              <ul className="space-y-1.5">
                {links.map((link) => (
                  <li
                    key={link.id}
                    className="flex items-center justify-between gap-2 px-3 py-2 rounded-lg bg-[#0f1115] border border-white/5"
                  >
                    <span className="text-xs text-gray-500 truncate">
                      Expires {new Date(link.expiresAt).toLocaleDateString()}
                      {link.hasPassword && " · Password protected"}
                    </span>
                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        onClick={() => copyLink(link)}
                        className="text-gray-500 hover:text-gray-200 transition-colors"
                        title="Copy link"
                      >
                        {copiedId === link.id ? <Check size={13} className="text-emerald-400" /> : <Copy size={13} />}
                      </button>
                      <button
                        onClick={() => handleRevokeLink(link.id)}
                        className="text-gray-600 hover:text-red-400 transition-colors"
                        title="Revoke link"
                      >
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
