@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES: JAVA OOP PRACTICAL NO. 04 ^& TASK 4 TEST SUITE
echo   Course Outcome: CO4 - File Handling ^& Java Generics
echo =======================================================================
echo.

set "JDK_JAVA="

:: 1. Prioritize bundled portable OpenJDK
if exist "jdk\bin\java.exe" (
    set "JDK_JAVA=jdk\bin\java.exe"
    goto :FOUND_JAVA
)

if exist "..\retro_fighter_2d\jdk\bin\java.exe" (
    set "JDK_JAVA=..\retro_fighter_2d\jdk\bin\java.exe"
    goto :FOUND_JAVA
)

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
    set "JDK_JAVA=%JAVA_HOME%\bin\java.exe"
    goto :FOUND_JAVA
)

if exist "C:\Program Files\Java\latest\jdk-27\bin\java.exe" (
    set "JDK_JAVA=C:\Program Files\Java\latest\jdk-27\bin\java.exe"
    goto :FOUND_JAVA
)

where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "JDK_JAVA=java"
    goto :FOUND_JAVA
)

echo [!] Java runtime was not found in system PATH or bundled jdk folder.
echo     Please ensure Java is installed or bundled with the project.
pause
exit /b 1

:FOUND_JAVA
if not exist "4_Object_Oriented_Programming_Java\bin\CO4\Testing\CO4PracticalTestSuite.class" (
    echo [*] Compiling Java OOP classes...
    call build.bat
)

echo [*] Using Java runtime: "%JDK_JAVA%"
echo [*] Executing CO4 Automated Practical Test Suite...
echo.

"%JDK_JAVA%" -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" CO4.Testing.CO4PracticalTestSuite %*

echo.
pause
