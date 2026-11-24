@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES : SUPABASE CLOUD AUTHENTICATION PORTAL (JAVA SWING)
echo =======================================================================
echo.

:: Auto-detect Java runtime
set "JAVA_CMD="
if exist "jdk\bin\java.exe" (
    set "JAVA_CMD=jdk\bin\java.exe"
) else if exist "..\retro_fighter_2d\jdk\bin\java.exe" (
    set "JAVA_CMD=..\retro_fighter_2d\jdk\bin\java.exe"
) else if exist "%USERPROFILE%\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe" (
    set "JAVA_CMD=%USERPROFILE%\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
) else (
    where java >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        set "JAVA_CMD=java"
    )
)

if not defined JAVA_CMD (
    echo [!] Java runtime was not found in system PATH.
    echo.
    pause
    exit /b 1
)

echo [*] Launching Supabase Cloud Authentication GUI...
start "" "%JAVA_CMD%" -cp 4_Object_Oriented_Programming_Java\bin CO3.Auth.SupabaseAuthGUI %*
