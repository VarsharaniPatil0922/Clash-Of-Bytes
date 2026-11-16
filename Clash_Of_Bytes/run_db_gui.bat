@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES : TWO-PLAYER DATABASE AUTH & MATCH HISTORY SYSTEM
echo   Subjects: Problem Solving Using OOP Java + Relational JDBC Database
echo =======================================================================
echo.

:: Auto-detect Java runtime
set "JAVA_CMD="
if exist "C:\Program Files\Java\latest\jdk-27\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\latest\jdk-27\bin\java.exe"
) else if exist "jdk\bin\java.exe" (
    set "JAVA_CMD=jdk\bin\java.exe"
) else if exist "..\retro_fighter_2d\jdk\bin\java.exe" (
    set "JAVA_CMD=..\retro_fighter_2d\jdk\bin\java.exe"
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

if not exist "4_Object_Oriented_Programming_Java\bin\ui\TwoPlayerLoginFrame.class" (
    echo [*] Classes not compiled yet. Running build.bat first...
    call .\build.bat
)

echo [*] Launching Two-Player Database Authentication on Event Dispatch Thread...
start "" "%JAVA_CMD%" -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" ui.TwoPlayerLoginFrame %*
