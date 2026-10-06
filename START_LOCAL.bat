@echo off
setlocal
cd /d "%~dp0backend"

echo ================================================
echo Student Attendance Management System
echo ================================================
echo.

where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java is not installed or not on PATH.
  echo Install Java 21, then run this file again.
  pause
  exit /b 1
)

if not exist "target\student-attendance-api-0.0.1-SNAPSHOT.jar" (
  echo First run: building the backend...
  where mvn >nul 2>&1
  if errorlevel 1 (
    echo ERROR: Maven is not installed or not on PATH.
    echo Install Maven, then run this file again.
    pause
    exit /b 1
  )
  call mvn -DskipTests package
  if errorlevel 1 (
    echo.
    echo BUILD FAILED. Read the Maven error above.
    pause
    exit /b 1
  )
)

echo Starting the application...
start "Student Attendance Backend" /D "%~dp0backend" cmd /k java -jar target\student-attendance-api-0.0.1-SNAPSHOT.jar

echo Waiting for Spring Boot to start...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$deadline=(Get-Date).AddSeconds(60); while((Get-Date) -lt $deadline){ try { $r=Invoke-WebRequest -Uri 'http://localhost:8080/health' -UseBasicParsing -TimeoutSec 2; if($r.StatusCode -eq 200){ Start-Process 'http://localhost:8080/'; exit 0 } } catch {} ; Start-Sleep -Seconds 2 }; Start-Process 'http://localhost:8080/'"

echo.
echo Website: http://localhost:8080/
echo Keep the backend window open while using the system.
echo.
pause
