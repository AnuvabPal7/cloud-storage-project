import { useState, useEffect, useCallback, useRef } from "react";
import { Plus, Upload as UploadIcon, LogOut, UploadCloud, Share2, Trash2, RotateCcw, Menu } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import Sidebar from "../components/Sidebar";
import Breadcrumbs from "../components/Breadcrumbs";
import FileGrid from "../components/FileGrid";
import CreateFolderModal from "../components/CreateFolderModal";
import UploadQueue from "../components/UploadQueue";
import FilePreviewModal from "../components/FilePreviewModal";
import ShareModal from "../components/ShareModal";
import SearchBar from "../components/SearchBar";
import SortControl from "../components/SortControl";
import Pagination from "../components/Pagination";
import { getFolderContents, createFolder, deleteFolder } from "../api/folders";
import {
  uploadFile,
  getFile,
  MAX_FILE_SIZE_BYTES,
  softDeleteFile,
  restoreFile,
  permanentlyDeleteFile,
  listTrash,
  searchFiles,
} from "../api/files";
import { listSharedWithMe, getSharedDownloadUrl } from "../api/shares";
import { formatFileSize } from "../utils/format";

let uploadIdCounter = 0;

export default function Dashboard() {
  const { user, logout } = useAuth();

  // "drive" | "shared" | "trash" - which sidebar section is active.
  const [viewMode, setViewMode] = useState("drive");
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  // --- Drive (folder browsing) state ---
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [path, setPath] = useState([]);
  const [subfolders, setSubfolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [driveLoading, setDriveLoading] = useState(true);

  // --- Shared / Trash (flat list) state ---
  const [sharedFiles, setSharedFiles] = useState([]);
  const [trashFiles, setTrashFiles] = useState([]);
  const [flatLoading, setFlatLoading] = useState(true);

  // --- Search state (overrides whatever viewMode shows, while active) ---
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState(null); // Page<FileResponse> | null
  const [searchPage, setSearchPage] = useState(0);
  const [sortOption, setSortOption] = useState({ value: "createdAt_desc", sortBy: "createdAt", sortDir: "desc" });
  const [searchLoading, setSearchLoading] = useState(false);

  const [error, setError] = useState("");

  const [showCreateFolder, setShowCreateFolder] = useState(false);
  const [uploads, setUploads] = useState([]);
  const [isDragging, setIsDragging] = useState(false);
  const dragCounter = useRef(0);
  const fileInputRef = useRef(null);

  const [preview, setPreview] = useState(null); // { file, loading, error } | null
  const [shareModalFile, setShareModalFile] = useState(null);

  const isSearching = searchQuery.trim().length > 0;

  // --- Loaders ---

  const loadDrive = useCallback(async (folderId) => {
    setDriveLoading(true);
    setError("");
    try {
      const data = await getFolderContents(folderId);
      setSubfolders(data.subfolders);
      setFiles(data.files);
    } catch {
      setError("Couldn't load this folder. Try refreshing.");
    } finally {
      setDriveLoading(false);
    }
  }, []);

  const loadShared = useCallback(async () => {
    setFlatLoading(true);
    setError("");
    try {
      setSharedFiles(await listSharedWithMe());
    } catch {
      setError("Couldn't load shared files.");
    } finally {
      setFlatLoading(false);
    }
  }, []);

  const loadTrash = useCallback(async () => {
    setFlatLoading(true);
    setError("");
    try {
      setTrashFiles(await listTrash());
    } catch {
      setError("Couldn't load trash.");
    } finally {
      setFlatLoading(false);
    }
  }, []);

  useEffect(() => {
    if (viewMode === "drive") loadDrive(currentFolderId);
  }, [viewMode, currentFolderId, loadDrive]);

  useEffect(() => {
    if (viewMode === "shared") loadShared();
  }, [viewMode, loadShared]);

  useEffect(() => {
    if (viewMode === "trash") loadTrash();
  }, [viewMode, loadTrash]);

  useEffect(() => {
    if (!isSearching) {
      setSearchResults(null);
      return;
    }
    setSearchLoading(true);
    searchFiles({ query: searchQuery, page: searchPage, sortBy: sortOption.sortBy, sortDir: sortOption.sortDir })
      .then(setSearchResults)
      .catch(() => setError("Search failed. Try again."))
      .finally(() => setSearchLoading(false));
  }, [isSearching, searchQuery, searchPage, sortOption]);

  // --- Navigation ---

  function handleSidebarNavigate(key) {
    setViewMode(key);
    setSearchQuery("");
    setSearchPage(0);
    setMobileSidebarOpen(false);
    if (key === "drive") {
      setCurrentFolderId(null);
      setPath([]);
    }
  }

  function handleOpenFolder(folder) {
    setPath((prev) => [...prev, { id: folder.id, name: folder.name }]);
    setCurrentFolderId(folder.id);
  }

  // Clicking a folder in search results jumps straight into Drive at that
  // folder. We don't know its full ancestor chain (search doesn't return
  // that), so the breadcrumb just shows "My Drive > FolderName" even if
  // it's nested deeper - a reasonable simplification rather than fetching
  // the whole path just for the breadcrumb display.
  function handleOpenFolderFromSearch(folder) {
    setSearchQuery("");
    setViewMode("drive");
    setPath([{ id: folder.id, name: folder.name }]);
    setCurrentFolderId(folder.id);
  }

  function handleBreadcrumbNavigate(folderId, newPath) {
    setPath(newPath);
    setCurrentFolderId(folderId);
  }

  function handleSearch(query) {
    setSearchQuery(query);
    setSearchPage(0);
  }

  // --- File preview ---

  async function handleOpenFile(file) {
    setPreview({ file: { ...file, downloadUrl: null }, loading: true, error: null });
    try {
      const data = await getFile(file.id);
      setPreview({ file: data, loading: false, error: null });
    } catch {
      setPreview({ file, loading: false, error: "Couldn't load this file." });
    }
  }

  async function handleOpenSharedFile(file) {
    setPreview({ file: { ...file, id: file.fileId, downloadUrl: null }, loading: true, error: null });
    try {
      const data = await getSharedDownloadUrl(file.fileId);
      setPreview({ file: data, loading: false, error: null });
    } catch {
      setPreview({ file, loading: false, error: "Couldn't load this file." });
    }
  }

  // --- Folder actions ---

  async function handleCreateFolder(name) {
    await createFolder(name, currentFolderId);
    await loadDrive(currentFolderId);
  }

  async function handleDeleteFolder(folder) {
    try {
      await deleteFolder(folder.id);
      await loadDrive(currentFolderId);
    } catch (err) {
      setError(err.response?.data?.error || "Couldn't delete that folder.");
    }
  }

  // --- File actions (drive view) ---

  async function handleSoftDelete(file) {
    try {
      await softDeleteFile(file.id);
      await loadDrive(currentFolderId);
    } catch (err) {
      setError(err.response?.data?.error || "Couldn't delete that file.");
    }
  }

  // --- File actions (trash view) ---

  async function handleRestore(file) {
    try {
      await restoreFile(file.id);
      await loadTrash();
    } catch (err) {
      setError(err.response?.data?.error || "Couldn't restore that file.");
    }
  }

  async function handlePermanentDelete(file) {
    if (!window.confirm(`Permanently delete "${file.name}"? This can't be undone.`)) return;
    try {
      await permanentlyDeleteFile(file.id);
      await loadTrash();
    } catch (err) {
      setError(err.response?.data?.error || "Couldn't permanently delete that file.");
    }
  }

  // --- Upload handling ---

  function startUpload(file) {
    const id = ++uploadIdCounter;

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setUploads((prev) => [
        ...prev,
        { id, name: file.name, progress: 0, status: "error", error: `Too large (max ${formatFileSize(MAX_FILE_SIZE_BYTES)})` },
      ]);
      return;
    }

    setUploads((prev) => [...prev, { id, name: file.name, progress: 0, status: "uploading" }]);

    uploadFile(file, currentFolderId, (progress) => {
      setUploads((prev) => prev.map((u) => (u.id === id ? { ...u, progress } : u)));
    })
      .then(() => {
        setUploads((prev) => prev.map((u) => (u.id === id ? { ...u, status: "done", progress: 100 } : u)));
        loadDrive(currentFolderId);
      })
      .catch((err) => {
        const message = err.response?.data?.error || "Upload failed";
        setUploads((prev) => prev.map((u) => (u.id === id ? { ...u, status: "error", error: message } : u)));
      });
  }

  function handleUploadClick() {
    fileInputRef.current?.click();
  }

  function handleFileSelected(e) {
    const selected = Array.from(e.target.files || []);
    e.target.value = "";
    selected.forEach(startUpload);
  }

  function dismissUpload(id) {
    setUploads((prev) => prev.filter((u) => u.id !== id));
  }

  function handleDragEnter(e) {
    e.preventDefault();
    dragCounter.current += 1;
    setIsDragging(true);
  }
  function handleDragLeave(e) {
    e.preventDefault();
    dragCounter.current -= 1;
    if (dragCounter.current <= 0) {
      dragCounter.current = 0;
      setIsDragging(false);
    }
  }
  function handleDragOver(e) {
    e.preventDefault();
  }
  function handleDrop(e) {
    e.preventDefault();
    dragCounter.current = 0;
    setIsDragging(false);
    Array.from(e.dataTransfer.files || []).forEach(startUpload);
  }

  const canUploadHere = viewMode === "drive" && !isSearching;

  // --- Action menu builders ---

  function driveFolderActions(folder) {
    return [{ label: "Delete", icon: Trash2, danger: true, onClick: () => handleDeleteFolder(folder) }];
  }

  function driveFileActions(file) {
    return [
      { label: "Share", icon: Share2, onClick: () => setShareModalFile(file) },
      { label: "Delete", icon: Trash2, danger: true, onClick: () => handleSoftDelete(file) },
    ];
  }

  function trashFileActions(file) {
    return [
      { label: "Restore", icon: RotateCcw, onClick: () => handleRestore(file) },
      { label: "Delete forever", icon: Trash2, danger: true, onClick: () => handlePermanentDelete(file) },
    ];
  }

  // --- What to render in the main area ---

  let mainContent;
  if (isSearching) {
    mainContent = (
      <>
        <FileGrid
          subfolders={searchResults?.folders || []}
          files={searchResults?.files?.content || []}
          onOpenFolder={handleOpenFolderFromSearch}
          onOpenFile={handleOpenFile}
          loading={searchLoading}
          fileActions={driveFileActions}
          emptyMessage="Nothing matches your search"
        />
        {searchResults?.files && (
          <Pagination
            page={searchResults.files.number}
            totalPages={searchResults.files.totalPages}
            onPageChange={setSearchPage}
          />
        )}
      </>
    );
  } else if (viewMode === "shared") {
    mainContent = (
      <FileGrid
        subfolders={[]}
        files={sharedFiles.map((f) => ({ ...f, id: f.fileId }))}
        onOpenFolder={() => {}}
        onOpenFile={handleOpenSharedFile}
        loading={flatLoading}
        emptyMessage="Nothing has been shared with you yet"
        fileSubtitle={(f) => `Shared by ${f.ownerEmail} · ${f.permission}`}
      />
    );
  } else if (viewMode === "trash") {
    mainContent = (
      <FileGrid
        subfolders={[]}
        files={trashFiles}
        onOpenFolder={() => {}}
        onOpenFile={() => {}}
        loading={flatLoading}
        fileActions={trashFileActions}
        emptyMessage="Trash is empty"
      />
    );
  } else {
    mainContent = (
      <FileGrid
        subfolders={subfolders}
        files={files}
        onOpenFolder={handleOpenFolder}
        onOpenFile={handleOpenFile}
        loading={driveLoading}
        folderActions={driveFolderActions}
        fileActions={driveFileActions}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[#0f1115] flex">
      {/* Mobile sidebar overlay */}
      {mobileSidebarOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div className="absolute inset-0 bg-black/60" onClick={() => setMobileSidebarOpen(false)} />
          <div className="absolute left-0 top-0 bottom-0 bg-[#0f1115]">
            <Sidebar active={viewMode} onNavigate={handleSidebarNavigate} />
          </div>
        </div>
      )}

      {/* Desktop sidebar */}
      <div className="hidden md:block">
        <Sidebar active={viewMode} onNavigate={handleSidebarNavigate} />
      </div>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="border-b border-white/5 px-4 sm:px-6 py-3 flex flex-wrap items-center gap-3 sm:gap-4">
          <button
            onClick={() => setMobileSidebarOpen(true)}
            className="md:hidden p-1.5 rounded-lg text-gray-400 hover:bg-white/5 shrink-0"
          >
            <Menu size={18} />
          </button>

          <div className="flex-1 min-w-0">
            {viewMode === "drive" && !isSearching ? (
              <Breadcrumbs path={path} onNavigate={handleBreadcrumbNavigate} />
            ) : (
              <span className="text-sm font-medium text-gray-200">
                {isSearching ? "Search results" : viewMode === "shared" ? "Shared with me" : "Trash"}
              </span>
            )}
          </div>

          <SearchBar onSearch={handleSearch} />
          {isSearching && <SortControl value={sortOption.value} onChange={setSortOption} />}

          <div className="flex items-center gap-2 shrink-0">
            {canUploadHere && (
              <>
                <button
                  onClick={() => setShowCreateFolder(true)}
                  className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-gray-300
                             border border-white/10 hover:bg-white/5 transition-colors"
                >
                  <Plus size={14} />
                  New folder
                </button>
                <button
                  onClick={handleUploadClick}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-white
                             bg-indigo-500 hover:bg-indigo-400 transition-colors"
                >
                  <UploadIcon size={14} />
                  <span className="hidden sm:inline">Upload</span>
                </button>
                <input ref={fileInputRef} type="file" multiple className="hidden" onChange={handleFileSelected} />
              </>
            )}

            <div className="w-px h-5 bg-white/10 mx-1 hidden sm:block" />

            <span className="text-xs text-gray-500 hidden lg:inline">{user?.name}</span>
            <button
              onClick={logout}
              className="p-1.5 rounded-lg text-gray-500 hover:text-gray-200 hover:bg-white/5 transition-colors"
              title="Log out"
            >
              <LogOut size={16} />
            </button>
          </div>
        </header>

        <main
          className="flex-1 px-4 sm:px-6 py-5 overflow-auto relative"
          onDragEnter={canUploadHere ? handleDragEnter : undefined}
          onDragLeave={canUploadHere ? handleDragLeave : undefined}
          onDragOver={canUploadHere ? handleDragOver : undefined}
          onDrop={canUploadHere ? handleDrop : undefined}
        >
          {isDragging && canUploadHere && (
            <div className="absolute inset-3 z-30 rounded-2xl border-2 border-dashed border-indigo-500/50 bg-indigo-500/5 flex flex-col items-center justify-center pointer-events-none">
              <UploadCloud size={36} className="text-indigo-400 mb-2" />
              <p className="text-sm text-indigo-300 font-medium">Drop to upload</p>
            </div>
          )}

          {error && (
            <div className="mb-4 px-3 py-2.5 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-sm">
              {error}
            </div>
          )}

          {mainContent}
        </main>
      </div>

      {showCreateFolder && (
        <CreateFolderModal onClose={() => setShowCreateFolder(false)} onCreate={handleCreateFolder} />
      )}

      {preview && (
        <FilePreviewModal
          file={preview.file}
          loading={preview.loading}
          error={preview.error}
          onClose={() => setPreview(null)}
        />
      )}

      {shareModalFile && (
        <ShareModal file={shareModalFile} onClose={() => setShareModalFile(null)} />
      )}

      <UploadQueue uploads={uploads} onDismiss={dismissUpload} />
    </div>
  );
}
