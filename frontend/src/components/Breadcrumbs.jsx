import { ChevronRight } from "lucide-react";

// path is an array of {id, name} from root to current folder (root itself
// is NOT included here - "My Drive" is rendered separately below so it's
// always clickable even when path is empty).
export default function Breadcrumbs({ path, onNavigate }) {
  return (
    <div className="flex items-center gap-1 text-sm min-w-0">
      <button
        onClick={() => onNavigate(null, [])}
        className={`px-1.5 py-1 rounded hover:bg-white/5 transition-colors ${
          path.length === 0 ? "text-gray-200 font-medium" : "text-gray-500 hover:text-gray-300"
        }`}
      >
        My Drive
      </button>

      {path.map((crumb, index) => {
        const isLast = index === path.length - 1;
        return (
          <span key={crumb.id} className="flex items-center gap-1 min-w-0">
            <ChevronRight size={14} className="text-gray-700 shrink-0" />
            <button
              onClick={() => onNavigate(crumb.id, path.slice(0, index + 1))}
              className={`px-1.5 py-1 rounded hover:bg-white/5 transition-colors truncate ${
                isLast ? "text-gray-200 font-medium" : "text-gray-500 hover:text-gray-300"
              }`}
            >
              {crumb.name}
            </button>
          </span>
        );
      })}
    </div>
  );
}
