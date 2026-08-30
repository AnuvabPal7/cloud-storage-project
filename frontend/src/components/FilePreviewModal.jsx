import { X, Download, Loader2, AlertCircle } from "lucide-react";
import { formatFileSize } from "../utils/format";

// file = { name, mimeType, size, downloadUrl } - downloadUrl may be null
// while it's still loading (parent fetches it async before opening this).
export default function FilePreviewModal({ file, loading, error, onClose }) {
  const isImage = file?.mimeType?.startsWith("image/");
  const isPdf = file?.mimeType === "application/pdf";

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 px-4" onClick={onClose}>
      <div
        className="w-full max-w-3xl max-h-[85vh] bg-[#16181f] border border-white/10 rounded-2xl shadow-2xl flex flex-col overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b border-white/5 shrink-0">
          <div className="min-w-0">
            <p className="text-sm text-gray-100 truncate" title={file?.name}>
              {file?.name}
            </p>
            {file?.size != null && (
              <p className="text-[11px] text-gray-600">{formatFileSize(file.size)}</p>
            )}
          </div>
          <div className="flex items-center gap-1 shrink-0">
            {file?.downloadUrl && (
              <a
                href={file.downloadUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="p-1.5 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-white/5 transition-colors"
                title="Download"
              >
                <Download size={16} />
              </a>
            )}
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-gray-400 hover:text-gray-200 hover:bg-white/5 transition-colors"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        <div className="flex-1 overflow-auto bg-[#0a0b0e] flex items-center justify-center min-h-[300px]">
          {loading && <Loader2 size={28} className="text-gray-600 animate-spin" />}

          {!loading && error && (
            <div className="flex flex-col items-center gap-2 text-center px-6">
              <AlertCircle size={28} className="text-red-400" />
              <p className="text-sm text-gray-400">{error}</p>
            </div>
          )}

          {!loading && !error && file?.downloadUrl && isImage && (
            <img
              src={file.downloadUrl}
              alt={file.name}
              className="max-w-full max-h-[70vh] object-contain"
            />
          )}

          {!loading && !error && file?.downloadUrl && isPdf && (
            <iframe
              src={file.downloadUrl}
              title={file.name}
              className="w-full h-[70vh]"
            />
          )}

          {!loading && !error && file?.downloadUrl && !isImage && !isPdf && (
            <div className="flex flex-col items-center gap-3 text-center px-6">
              <p className="text-sm text-gray-500">No preview available for this file type.</p>
              <a
                href={file.downloadUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="px-3.5 py-2 rounded-lg bg-indigo-500 hover:bg-indigo-400 text-white text-xs font-medium transition-colors"
              >
                Open file
              </a>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
