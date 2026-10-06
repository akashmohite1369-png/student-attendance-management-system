# Student Attendance Management System

Mini-project for Engineering Semester 03 attendance management.

## Features
- Separate Student and Teacher accounts
- Student/Teacher registration and BCrypt password login
- Teacher manual attendance by subject/date
- Teacher-generated 4-digit session codes such as `AOA-4821`
- Student check-in with a valid session code
- Subject-wise attendance, eligibility, and reports
- Spring Boot + H2 backend
- Netlify frontend + Render backend ready

## Local run
From the backend folder:
```powershell
cd backend
mvn clean package
mvn spring-boot:run
```
Then open `frontend/index.html` with VS Code Live Server.

The H2 database is created automatically in `backend/data/`. The distributed ZIP intentionally does not include runtime DB or Maven `target/` files.

## Demo accounts
Student: `student@college.edu` / `student123`
Teacher: `teacher@college.edu` / `teacher123`

New students and teachers can create their own accounts from the login screen.

## API
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/subjects`
- `GET /api/students`
- `GET /api/attendance/summary?studentId=1`
- `GET /api/attendance/report?subjectCode=AOA`
- `POST /api/attendance`
- `POST /api/sessions/generate`
- `GET /api/sessions/active?teacherUserId=1`
- `POST /api/sessions/check-in`

## H2 console
`http://localhost:8080/h2-console`

JDBC URL: `jdbc:h2:file:./data/attendance_db`
User: `sa`
Password: blank
