import { useState, useEffect } from "react";
import { HardDrive, Users, Trash2 } from "lucide-react";
import { getStorageUsage } from "../api/files";
import { formatFileSize } from "../utils/format";

const NAV_ITEMS = [
  { key: "drive", label: "My Drive", icon: HardDrive },
  { key: "shared", label: "Shared with me", icon: Users },
  { key: "trash", label: "Trash", icon: Trash2 },
];

export default function Sidebar({ active, onNavigate }) {
  const [usage, setUsage] = useState(null); // { usedBytes, limitBytes } | null

  // Fetched once when the sidebar mounts - matches how most drive-style
  // apps show this (not a live-updating meter after every single upload).
  // A page refresh or re-navigating to the dashboard picks up the latest number.
  useEffect(() => {
    getStorageUsage()
      .then(setUsage)
      .catch(() => {
        /* non-fatal - the sidebar still works fine without the usage bar */
      });
  }, []);

  const percentUsed = usage ? Math.min(100, (usage.usedBytes / usage.limitBytes) * 100) : 0;

  return (
    <aside className="w-56 shrink-0 border-r border-white/5 px-3 py-4 flex flex-col justify-between">
      <nav className="space-y-0.5">
        {NAV_ITEMS.map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => onNavigate(key)}
            className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-left transition-colors
              ${
                active === key
                  ? "bg-indigo-500/10 text-indigo-300"
                  : "text-gray-400 hover:bg-white/5 hover:text-gray-200"
              }`}
          >
            <Icon size={16} strokeWidth={1.8} />
            {label}
          </button>
        ))}
      </nav>

      {usage && (
        <div className="px-3 pt-3 border-t border-white/5">
          <div className="h-1 bg-white/5 rounded-full overflow-hidden mb-1.5">
            <div
              className={`h-full transition-all duration-300 ${
                percentUsed > 90 ? "bg-red-500" : percentUsed > 70 ? "bg-amber-500" : "bg-indigo-500"
              }`}
              style={{ width: `${percentUsed}%` }}
            />
          </div>
          <p className="text-[11px] text-gray-500">
            {formatFileSize(usage.usedBytes)} of {formatFileSize(usage.limitBytes)} used
          </p>
        </div>
      )}
    </aside>
  );
}
