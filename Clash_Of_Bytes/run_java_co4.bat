@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES: JAVA OOP PRACTICAL NO. 04 ^& TASK 4 TEST SUITE
echo   Course Outcome: CO4 - File Handling ^& Java Generics
echo =======================================================================
echo.

set "JDK_JAVA="

if exist "C:\Program Files\Java\latest\jdk-27\bin\java.exe" (
    set "JDK_JAVA=C:\Program Files\Java\latest\jdk-27\bin\java.exe"
    goto :FOUND_JAVA
)

if exist "jdk\bin\java.exe" (
    set "JDK_JAVA=jdk\bin\java.exe"
    goto :FOUND_JAVA
)

where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "JDK_JAVA=java"
    goto :FOUND_JAVA
)

echo [!] Java runtime was not found in system PATH.
pause
exit /b 1

:FOUND_JAVA
"%JDK_JAVA%" -cp "4_Object_Oriented_Programming_Java\bin;4_Object_Oriented_Programming_Java\lib\*" CO4.Testing.CO4PracticalTestSuite %*
pause
