# Cloud Storage — Full-Stack File Storage & Sharing App

A Google Drive-inspired cloud storage application built from scratch — file upload/download, nested folders, sharing with permission levels, public links, search, trash, and a polished dark-mode UI. Built as a portfolio project to demonstrate full-stack engineering across a real, deployed system.

**Live app:** https://cloud-storage-project-two.vercel.app
**Live API:** https://cloud-storage-project-tw3q.onrender.com
**Repo:** https://github.com/AnuvabPal7/cloud-storage-project

> Backend free tier note: the API sleeps after 15 minutes of inactivity (Render free tier). The first request after idle time can take 30–60 seconds to wake up — this is expected, not a bug.

---

## Features

**Authentication**
- JWT-based register/login, BCrypt password hashing, protected routes

**Files & Folders**
- Nested folder hierarchy, breadcrumb navigation
- Upload via button or drag-and-drop, with live progress bars
- In-app preview for images and PDFs
- Rename, move between folders
- Soft-delete to Trash, restore, and permanent delete (two-step safety)

**Sharing**
- Share files with specific users by email, with Viewer/Editor permission levels
- Public share links with optional expiry and password protection
- "Shared with me" view

**Search**
- Unified search across files and folders
- Filter by file type, sort by name/size/date, paginated results

**Other**
- Dark mode, mobile-responsive layout
- Deployed with Docker (backend) and Vercel (frontend)

---

## Tech Stack

**Backend**
- Java 21, Spring Boot 4, Spring Security, Spring Data JPA / Hibernate
- PostgreSQL (via Supabase), Supabase Storage for files
- JWT auth (jjwt), Maven, Docker
- Deployed on Render

**Frontend**
- React 19, Vite, Tailwind CSS v4
- React Router, Axios, Lucide icons
- Deployed on Vercel

---

## Project Structure

```
cloud-storage-project/
├── src/main/java/com/cloudstorage/backend/
│   ├── controller/    REST endpoints
│   ├── service/       business logic
│   ├── repository/    Spring Data JPA repositories
│   ├── model/          JPA entities
│   ├── dto/            request/response records
│   ├── security/       JWT auth, Spring Security config
│   ├── storage/         Supabase Storage integration
│   └── exception/       global error handling
├── frontend/
│   └── src/
│       ├── api/          axios calls to the backend
│       ├── components/   reusable UI pieces
│       ├── pages/         route-level views
│       └── context/       auth state
├── postman/               importable API test collection
├── Dockerfile              backend container build
├── RENDER_DEPLOYMENT.md
└── frontend/VERCEL_DEPLOYMENT.md
```

---

## Running Locally

**Backend**
```bash
mvn spring-boot:run
```
Requires these environment variables set locally: `CS_PASSWORD` (Supabase DB password), `JWT_SECRET`, `SUPABASE_SERVICE_ROLE_KEY`.

**Frontend**
```bash
cd frontend
npm install
npm run dev
```
Requires `VITE_API_BASE_URL` in `frontend/.env` (defaults to `http://localhost:8080`).

---

## API Testing

A full Postman collection covering every endpoint (auth, files, folders, sharing, public links) is included at `postman/cloud-storage-project.postman_collection.json` — import it, run **Auth → Login**, and the JWT token auto-fills for every other request in the collection.

---

## Known Limitations

- Editor permission on shared files isn't yet wired into every file-management endpoint (rename/move/delete remain owner-only for now)
- File size capped at 50MB (Supabase free-tier storage limit)
- Search results show a simplified breadcrumb for folders (doesn't reconstruct the full ancestor path)