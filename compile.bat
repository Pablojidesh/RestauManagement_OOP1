@echo off
cd /d "%~dp0"
echo ===================================================
echo Compiling Restaurant Management System
echo ===================================================
if not exist "bin" mkdir bin
javac -cp "lib/*;." -d bin src\com\restaurant\config\*.java src\com\restaurant\model\*.java src\com\restaurant\dao\*.java src\com\restaurant\service\*.java src\com\restaurant\ui\components\*.java src\com\restaurant\ui\*.java src\com\restaurant\*.java
javac -cp "lib/*;bin" -d bin test\com\restaurant\*.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)
pause
