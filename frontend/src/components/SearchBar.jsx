import { useState, useEffect, useRef } from "react";
import { Search, X } from "lucide-react";

// Debounces internally (300ms) so onSearch isn't called on every keystroke -
// avoids hammering the backend while the user is still typing.
export default function SearchBar({ onSearch }) {
  const [value, setValue] = useState("");
  const debounceRef = useRef(null);

  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      onSearch(value.trim());
    }, 300);
    return () => clearTimeout(debounceRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value]);

  function handleClear() {
    setValue("");
    onSearch("");
  }

  return (
    <div className="relative w-full max-w-xs">
      <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-600" />
      <input
        type="text"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="Search Drive"
        className="w-full pl-8 pr-8 py-1.5 rounded-lg bg-[#16181f] border border-white/10 text-gray-200 text-xs
                   placeholder:text-gray-600 focus:outline-none focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/50
                   transition-colors"
      />
      {value && (
        <button
          onClick={handleClear}
          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-300"
        >
          <X size={13} />
        </button>
      )}
    </div>
  );
}
