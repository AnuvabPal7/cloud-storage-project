const OPTIONS = [
  { value: "createdAt_desc", label: "Newest first", sortBy: "createdAt", sortDir: "desc" },
  { value: "createdAt_asc", label: "Oldest first", sortBy: "createdAt", sortDir: "asc" },
  { value: "name_asc", label: "Name (A-Z)", sortBy: "name", sortDir: "asc" },
  { value: "name_desc", label: "Name (Z-A)", sortBy: "name", sortDir: "desc" },
  { value: "size_desc", label: "Largest first", sortBy: "size", sortDir: "desc" },
  { value: "size_asc", label: "Smallest first", sortBy: "size", sortDir: "asc" },
];

export default function SortControl({ value, onChange }) {
  return (
    <select
      value={value}
      onChange={(e) => {
        const option = OPTIONS.find((o) => o.value === e.target.value);
        onChange(option);
      }}
      className="px-2.5 py-1.5 rounded-lg bg-[#16181f] border border-white/10 text-gray-300 text-xs
                 focus:outline-none focus:border-indigo-500/50 transition-colors"
    >
      {OPTIONS.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </select>
  );
}
