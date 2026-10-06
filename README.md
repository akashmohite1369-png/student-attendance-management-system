# Student Attendance Management System

A mini-project built with HTML/CSS/JavaScript, Spring Boot 3.3.5, Spring Data JPA, and H2.

## Accounts

- Students can register with name, roll number, college email, and password.
- Teachers can register with name, college email, and password.
- Login verifies the selected role and password against the H2 database.
- Passwords are stored as BCrypt hashes.
- The demo accounts remain available:
  - Student: `student@college.edu` / `student123`
  - Teacher: `teacher@college.edu` / `teacher123`

## Subjects

AOA, COA, MATHS-3, DSGT, FSGT, ESE, ED.

## Backend

Run from `backend`:

```bash
mvn spring-boot:run
```

The API runs at `http://localhost:8080` and uses H2 file storage at `./data/attendance_db`.

## Frontend

Open `frontend/index.html` locally or deploy the `frontend` folder to Netlify. The included `netlify.toml` proxies `/api/*` to the Render backend.
