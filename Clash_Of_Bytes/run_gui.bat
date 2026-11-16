@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES : JAVA SWING ARENA LAUNCHER (CO3 PRACTICAL)
echo =======================================================================
echo.

:: Auto-detect Java runtime
set "JAVA_CMD="
if exist "jdk\bin\java.exe" (
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
    echo [!] You can still play the arcade game directly by running 'run_game.bat'!
    echo.
    pause
    exit /b 1
)

if not exist "4_Object_Oriented_Programming_Java\bin\CO3\SwingGUI\GameLauncherGUI.class" (
    echo [*] GUI classes not compiled yet. Running build.bat first...
    call .\build.bat
)

echo [*] Launching Cyberpunk Swing GUI on Event Dispatch Thread...
start "" "%JAVA_CMD%" -cp 4_Object_Oriented_Programming_Java\bin CO3.SwingGUI.GameLauncherGUI %*
