# Deployment Guide

This project is set up for the following public architecture:

Browser → Netlify static frontend → Netlify `/api` proxy → Render Spring Boot API → H2 database.

## 1. Put the project on GitHub

Create a new GitHub repository and upload the entire contents of this folder (including `netlify.toml`, `render.yaml`, `Dockerfile`, `frontend`, and `backend`).

Do not commit passwords or other secrets.

## 2. Deploy the Java backend to Render

In Render, choose New → Web Service and connect your GitHub repository. The repository contains a `render.yaml` Blueprint configuration and a Dockerfile. You can also create the Web Service manually and choose Docker.

For the demo/free deployment:
- Runtime: Docker
- Dockerfile: `./Dockerfile`
- Docker context: repository root
- Health check: `/health`
- Plan: Free

After deployment, Render gives the backend a public URL such as:
`https://attendly-api.onrender.com`

Test:
`https://YOUR-BACKEND.onrender.com/health`

Expected JSON:
`{"status":"UP","service":"student-attendance-api"}`

Also test:
`https://YOUR-BACKEND.onrender.com/api/subjects`

## 3. Deploy the frontend to Netlify

Use Netlify's Import an existing project flow and connect the same GitHub repository.

Netlify settings:
- Base directory: leave blank (repository root)
- Publish directory: `frontend`
- Build command: leave blank

`netlify.toml` already contains the `/api/*` proxy to the current Render backend URL. If your Render URL changes, edit that one URL in `netlify.toml` and commit the change.

You do not need a `BACKEND_URL` environment variable for the current configuration.

## 4. Student accounts

The login screen includes **New student? Create an account**. A student enters:
- Full name
- Roll number
- College email
- Password
- Confirm password

The backend creates a Student profile and a STUDENT login account. Passwords are stored as BCrypt hashes, not plain text.

A student can then sign in from any device using the same email and password. Their dashboard is matched to their own student profile and attendance records.

## 5. Teacher account

Teacher accounts remain controlled by the backend seed data. The included presentation account is:

Email: `teacher@college.edu`
Password: `teacher123`

Teachers can mark attendance and view reports.

## 6. Demo student account

Email: `student@college.edu`
Password: `student123`

The demo account is kept so the system can be demonstrated immediately.

## 7. Important H2 / Render limitation

The included Render Blueprint uses the Free web-service plan because it is convenient for a mini-project demo. H2 data stored on the container filesystem can be lost after a restart, redeploy or spin-down when the service does not have persistent storage.

For a stable project with permanent attendance and account data, use a managed relational database or persistent storage. The Java application reads its database URL from the `DB_URL` environment variable, so the frontend does not need to change.

## 8. Local development

Backend:
```powershell
cd backend
mvn spring-boot:run
```

Frontend:
Use VS Code Live Server on `frontend/index.html`.

The frontend automatically uses `http://localhost:8080/api` when opened on localhost. On Netlify it uses `/api`, which Netlify proxies to your public backend.
