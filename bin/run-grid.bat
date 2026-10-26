@echo off
cd /d "%~dp0"
where docker >nul 2>nul
if errorlevel 1 (
 echo Docker is not on PATH. Start Docker Desktop and open a new Command Prompt.
 exit /b 1
)
docker compose up --build --abort-on-container-exit --exit-code-from test-runner
set "TEST_EXIT=%ERRORLEVEL%"
docker compose down
exit /b %TEST_EXIT%
