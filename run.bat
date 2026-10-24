@echo off
cd /d "%~dp0"
echo ===================================================
echo Starting The Royal Spice - Restaurant Management System
echo ===================================================
java -cp "lib/*;bin" com.restaurant.Main
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo An error occurred while launching. Press any key to exit.
    pause
)
