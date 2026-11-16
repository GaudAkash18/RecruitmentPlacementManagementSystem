@echo off
cd /d "%~dp0"
echo Starting Recruitment and Placement Management System...
echo Make sure MySQL Server is running and config.properties has your MySQL password.
mvn clean compile
if errorlevel 1 (
  echo Build failed. Check the error above.
  pause
  exit /b 1
)
mvn exec:java
pause
