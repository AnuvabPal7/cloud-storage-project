import { X, CheckCircle2, AlertCircle, Loader2 } from "lucide-react";

// uploads = array of { id, name, progress, status: 'uploading'|'done'|'error', error? }
// Renders as a fixed panel bottom-right, same idea as Google Drive's
// upload toast - stays out of the way but visible while things are in flight.
export default function UploadQueue({ uploads, onDismiss }) {
  if (uploads.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 w-80 bg-[#16181f] border border-white/10 rounded-xl shadow-2xl shadow-black/40 overflow-hidden z-40">
      <div className="px-4 py-2.5 border-b border-white/5 text-xs font-medium text-gray-400">
        Uploads
      </div>
      <div className="max-h-64 overflow-y-auto">
        {uploads.map((u) => (
          <div key={u.id} className="px-4 py-2.5 border-b border-white/5 last:border-b-0">
            <div className="flex items-center gap-2">
              {u.status === "uploading" && (
                <Loader2 size={14} className="text-indigo-400 animate-spin shrink-0" />
              )}
              {u.status === "done" && <CheckCircle2 size={14} className="text-emerald-400 shrink-0" />}
              {u.status === "error" && <AlertCircle size={14} className="text-red-400 shrink-0" />}

              <span className="text-xs text-gray-200 truncate flex-1" title={u.name}>
                {u.name}
              </span>

              {u.status !== "uploading" && (
                <button
                  onClick={() => onDismiss(u.id)}
                  className="text-gray-600 hover:text-gray-300 shrink-0"
                >
                  <X size={13} />
                </button>
              )}
            </div>

            {u.status === "uploading" && (
              <div className="mt-1.5 h-1 bg-white/5 rounded-full overflow-hidden">
                <div
                  className="h-full bg-indigo-500 transition-all duration-200"
                  style={{ width: `${u.progress}%` }}
                />
              </div>
            )}

            {u.status === "error" && (
              <p className="mt-1 text-[11px] text-red-400/80">{u.error}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
