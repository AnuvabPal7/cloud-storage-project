# Deploying to Vercel

## 1. Create the Project

1. Push this project to GitHub if you haven't already (it already is).
2. Go to https://vercel.com/dashboard → **Add New** → **Project**
3. Import your `cloud-storage-project` repo
4. **Important**: since the frontend lives in a subfolder, set:
   - **Root Directory**: `frontend`
   - Vercel should auto-detect it as a Vite project once you set this (Framework Preset: Vite)

## 2. Set the Environment Variable

Under **Environment Variables**, add:

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | Your Render backend URL, e.g. `https://cloud-storage-project-tw3q.onrender.com` |

No trailing slash on the URL.

## 3. Deploy

Click **Deploy**. Vercel builds and gives you a URL like
`https://your-project.vercel.app`.

## 4. Connect It Back to Your Backend (Important - Don't Skip)

Your backend's CORS config only allows specific origins. Right now it
doesn't know about your new Vercel URL, so every request from the deployed
frontend will fail with a CORS error even though both sides are technically
"working."

Go to **Render dashboard → your backend service → Environment**, and set:

| Key | Value |
|---|---|
| `CORS_ALLOWED_ORIGINS` | `https://your-project.vercel.app` |

If you want both local dev and production to keep working, use a
comma-separated list:
```
https://your-project.vercel.app,http://localhost:5173
```

Render will redeploy automatically when you save the env var change.

## 5. Verify

Visit your Vercel URL, sign up or log in, and confirm the whole flow works
end to end - same as testing locally, just against two real deployed
services instead of localhost.

## Known Gotchas

- **First backend request may be slow** - Render's free tier spins down
  after 15 minutes of inactivity. The first login attempt after a while
  might take 30-60 seconds. This is expected, not a bug.
- **Refreshing a page like `/dashboard` or `/share/:token` must NOT 404** -
  the included `vercel.json` handles this (routes all paths to
  `index.html` so React Router can take over client-side). If you ever
  see a 404 on refresh, check that `vercel.json` made it into the deployed
  build.
