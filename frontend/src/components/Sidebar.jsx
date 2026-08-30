import { HardDrive, Users, Trash2 } from "lucide-react";

const NAV_ITEMS = [
  { key: "drive", label: "My Drive", icon: HardDrive },
  { key: "shared", label: "Shared with me", icon: Users },
  { key: "trash", label: "Trash", icon: Trash2 },
];

export default function Sidebar({ active, onNavigate }) {
  return (
    <aside className="w-56 shrink-0 border-r border-white/5 px-3 py-4">
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
    </aside>
  );
}
