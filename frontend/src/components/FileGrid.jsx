import { Folder, FileText, Image as ImageIcon, FileArchive, File as FileIcon } from "lucide-react";
import { formatFileSize, formatDate } from "../utils/format";
import ItemMenu from "./ItemMenu";

function iconForMimeType(mimeType) {
  if (!mimeType) return FileIcon;
  if (mimeType.startsWith("image/")) return ImageIcon;
  if (mimeType === "application/pdf") return FileText;
  if (mimeType.includes("zip") || mimeType.includes("compressed")) return FileArchive;
  return FileIcon;
}

// folderActions/fileActions are optional functions: (item) => [{label, icon, onClick, danger}]
// If omitted, no kebab menu is shown for that item type - keeps simpler
// views (like a bare listing) from needing to wire up actions they don't use.
export default function FileGrid({
  subfolders = [],
  files = [],
  onOpenFolder,
  onOpenFile,
  loading,
  folderActions,
  fileActions,
  emptyMessage = "This folder is empty",
  fileSubtitle, // optional (file) => string, overrides the default "size · date" line
}) {
  if (loading) {
    return <p className="text-sm text-gray-600 px-1">Loading...</p>;
  }

  const isEmpty = subfolders.length === 0 && files.length === 0;
  if (isEmpty) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <Folder size={40} strokeWidth={1.2} className="text-gray-700 mb-3" />
        <p className="text-sm text-gray-500">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
      {subfolders.map((folder) => {
        const actions = folderActions?.(folder);
        return (
          <div
            key={folder.id}
            onDoubleClick={() => onOpenFolder(folder)}
            onClick={() => onOpenFolder(folder)}
            className="group relative flex flex-col items-start gap-2 p-3 rounded-xl border border-white/5 bg-[#16181f]
                       hover:bg-[#1b1e26] hover:border-white/10 transition-colors text-left cursor-pointer"
          >
            {actions && actions.length > 0 && (
              <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <ItemMenu actions={actions} />
              </div>
            )}
            <Folder size={28} strokeWidth={1.5} className="text-indigo-400" />
            <span className="text-xs text-gray-200 truncate w-full" title={folder.name}>
              {folder.name}
            </span>
          </div>
        );
      })}

      {files.map((file) => {
        const Icon = iconForMimeType(file.mimeType);
        const actions = fileActions?.(file);
        return (
          <div
            key={file.id}
            onClick={() => onOpenFile(file)}
            className="group relative flex flex-col items-start gap-2 p-3 rounded-xl border border-white/5 bg-[#16181f]
                       hover:bg-[#1b1e26] hover:border-white/10 transition-colors text-left cursor-pointer"
          >
            {actions && actions.length > 0 && (
              <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                <ItemMenu actions={actions} />
              </div>
            )}
            <Icon size={28} strokeWidth={1.5} className="text-gray-400" />
            <span className="text-xs text-gray-200 truncate w-full" title={file.name}>
              {file.name}
            </span>
            <span className="text-[10px] text-gray-600">
              {fileSubtitle ? fileSubtitle(file) : `${formatFileSize(file.size)} · ${formatDate(file.createdAt)}`}
            </span>
          </div>
        );
      })}
    </div>
  );
}
