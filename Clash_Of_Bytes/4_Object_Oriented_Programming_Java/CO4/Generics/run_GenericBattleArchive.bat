@echo off
cd /d "%~dp0..\..\..\"

set "JDK_JAVA="
if exist "jdk\bin\java.exe" set "JDK_JAVA=jdk\bin\java.exe"
if not defined JDK_JAVA if exist "..\retro_fighter_2d\jdk\bin\java.exe" set "JDK_JAVA=..\retro_fighter_2d\jdk\bin\java.exe"
if not defined JDK_JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JDK_JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JDK_JAVA where java >nul 2>nul && set "JDK_JAVA=java"

if not defined JDK_JAVA (
    echo [ERROR] Java runtime not found.
    pause
    exit /b 1
)

echo [*] Launching CO4 GenericBattleArchive...
"%JDK_JAVA%" -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" CO4.Generics.GenericBattleArchive %*
echo.
pause
