# Local one-click start

The easiest local demo is now a single file:

**START_LOCAL.bat**

Double-click it. On the first run it builds the Spring Boot application, starts the backend, waits for the health endpoint, and opens:

`http://localhost:8080/`

The frontend is also bundled inside the Spring Boot application, so Live Server is not required for the one-click local demo.

Requirements on Windows:
- Java 21
- Maven (only needed the first time, or when the source changes)

The local database is stored in `backend/data/attendance_db`.

The same `frontend` folder is kept for Netlify deployment. Its API URL logic supports both Netlify (`/api`) and VS Code Live Server (`http://localhost:8080/api`).
