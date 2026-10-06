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

## 3. Deploy the frontend to Netlify

Use Netlify's Import an existing project flow and connect the same GitHub repository.

Netlify settings:
- Base directory: leave blank (repository root)
- Publish directory: `frontend`
- Build command: use the one in `netlify.toml`, or leave it detected from the file.

Add a Netlify environment variable:
`BACKEND_URL=https://YOUR-BACKEND.onrender.com`

Deploy. Netlify will rewrite `/api/*` requests to the Render API while keeping the browser on the Netlify domain.

## 4. Friend access

Your friend only needs the final Netlify URL, for example:
`https://your-project-name.netlify.app`

They do not need VS Code, Java, Maven or H2 installed.

## 5. Demo credentials

Student:
- Email: `student@college.edu`
- Password: `student123`

Teacher:
- Email: `teacher@college.edu`
- Password: `teacher123`

## 6. Important H2 / Render limitation

The included Render Blueprint uses the Free web-service plan because it is convenient for a mini-project demo. Render Free web services have ephemeral local storage, so H2 data stored on the container filesystem can be lost after a restart, redeploy or spin-down. Free services also spin down after 15 minutes of inactivity and can take about a minute to start again.

For a stable project with permanent attendance data, use a managed relational database (for example Render Postgres) or move the H2 database onto a persistent disk on a paid Render service. The Java application already reads its database URL from the `DB_URL` environment variable, so that change does not require a frontend redesign.

## 7. Local development

Backend:
```powershell
cd backend
mvn spring-boot:run
```

Frontend:
Use VS Code Live Server on `frontend/index.html`.

The frontend automatically uses `http://localhost:8080/api` when opened on localhost. On Netlify it uses `/api`, which Netlify proxies to your public backend.
