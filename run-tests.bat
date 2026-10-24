@echo off
cd /d "%~dp0"
echo ===================================================
echo Running Verification Test Suite (44 Tests)
echo ===================================================
java -cp "lib/*;bin" com.restaurant.SystemVerificationTest
echo.
pause
