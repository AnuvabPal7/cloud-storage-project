import { useAuth } from "../context/AuthContext";

// Placeholder landing page after login - proves the whole auth flow works
// end to end (register/login -> token stored -> protected route loads ->
// backend recognizes the token). The actual file explorer UI (folders,
// uploads, the Google-Drive-style browsing view) is Day 9's scope.
export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-[#0f1115]">
      <header className="border-b border-white/5 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
            <svg viewBox="0 0 24 24" fill="none" className="w-4 h-4 text-indigo-400">
              <path
                d="M6 19a4 4 0 01-.5-7.97A5.5 5.5 0 0116.9 8.05 4.5 4.5 0 0118 17H6z"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </div>
          <span className="text-sm font-medium text-gray-200">My Drive</span>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-500">{user?.name}</span>
          <button
            onClick={logout}
            className="text-sm text-gray-400 hover:text-gray-200 px-3 py-1.5 rounded-lg hover:bg-white/5 transition-colors"
          >
            Log out
          </button>
        </div>
      </header>

      <main className="flex flex-col items-center justify-center px-6 py-24 text-center">
        <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mb-5">
          <svg viewBox="0 0 24 24" fill="none" className="w-7 h-7 text-indigo-400">
            <path
              d="M6 19a4 4 0 01-.5-7.97A5.5 5.5 0 0116.9 8.05 4.5 4.5 0 0118 17H6z"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>
        <h1 className="text-lg font-semibold text-gray-100 mb-1.5">
          You're signed in, {user?.name?.split(" ")[0]}
        </h1>
        <p className="text-sm text-gray-500 max-w-xs">
          The file explorer (folders, uploads, sharing) is coming together next -
          this confirms your login is fully wired up to the backend.
        </p>
      </main>
    </div>
  );
}
