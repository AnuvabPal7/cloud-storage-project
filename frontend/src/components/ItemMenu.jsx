import { useEffect, useRef, useState } from "react";
import { MoreVertical } from "lucide-react";

// A small "..." kebab menu, used on file/folder cards for actions like
// Share, Delete, Restore. Closes itself on outside click or Escape -
// standard dropdown behavior so it doesn't linger open awkwardly.
// actions = [{ label, icon: LucideIcon, onClick, danger? }]
export default function ItemMenu({ actions }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    function handleEscape(e) {
      if (e.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, []);

  if (!actions || actions.length === 0) return null;

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={(e) => {
          e.stopPropagation();
          setOpen((v) => !v);
        }}
        className="p-1 rounded-md text-gray-500 hover:text-gray-200 hover:bg-white/10 transition-colors"
      >
        <MoreVertical size={14} />
      </button>

      {open && (
        <div
          className="absolute right-0 top-7 z-20 w-40 bg-[#1b1e26] border border-white/10 rounded-lg shadow-xl py-1"
          onClick={(e) => e.stopPropagation()}
        >
          {actions.map(({ label, icon: Icon, onClick, danger }) => (
            <button
              key={label}
              onClick={() => {
                setOpen(false);
                onClick();
              }}
              className={`w-full flex items-center gap-2 px-3 py-1.5 text-xs text-left transition-colors
                ${danger ? "text-red-400 hover:bg-red-500/10" : "text-gray-300 hover:bg-white/5"}`}
            >
              {Icon && <Icon size={13} />}
              {label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
