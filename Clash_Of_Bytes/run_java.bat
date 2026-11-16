@echo off
cd /d "%~dp0"

set "JDK_JAVA="

if exist "C:\Program Files\Java\latest\jdk-27\bin\java.exe" (
    set "JDK_JAVA=C:\Program Files\Java\latest\jdk-27\bin\java.exe"
    goto :FOUND_JAVA
)

if exist "jdk\bin\java.exe" (
    set "JDK_JAVA=jdk\bin\java.exe"
    goto :FOUND_JAVA
)


if exist "..\retro_fighter_2d\jdk\bin\java.exe" (
    set "JDK_JAVA=..\retro_fighter_2d\jdk\bin\java.exe"
    goto :FOUND_JAVA
)

where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "JDK_JAVA=java"
    goto :FOUND_JAVA
)

echo [!] Java runtime was not found in system PATH.
echo [!] You can still play the arcade game directly by running 'run_game.bat'!
echo     (The arcade game clash_of_bytes.exe is standalone and requires no Java).
echo.
pause
exit /b 1

:FOUND_JAVA
if not exist "4_Object_Oriented_Programming_Java\bin\Main\CombatManager.class" (
    echo [*] Java classes not found. Running build.bat first...
    call .\build.bat
)

echo [*] Launching Java OOP Combat Manager (Practicals 01 to 04)...
"%JDK_JAVA%" -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" Main.CombatManager %*
pause
