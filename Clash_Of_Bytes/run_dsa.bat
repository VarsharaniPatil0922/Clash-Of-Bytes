@echo off
cd /d "%~dp0"

echo =======================================================================
echo   PROGRAMMING LABORATORY (DSA) - PRACTICALS 01 TO 07 and TASK 3 RUNNER
echo   Course Code: 2310214L
echo =======================================================================
echo.

if exist "C:\msys64\ucrt64\bin\g++.exe" set "PATH=C:\msys64\ucrt64\bin;C:\msys64\mingw64\bin;%PATH%"

if not exist "dsa_practicals.exe" (
    echo [*] Compiling DSA practicals test driver...
    g++ -std=c++17 -O2 -Wall 2_Programming_Laboratory_DSA/CO1_to_CO3_DSAManager.cpp -o dsa_practicals.exe
)

if exist "dsa_practicals.exe" (
    .\dsa_practicals.exe %*
) else (
    echo [ERROR] dsa_practicals.exe could not be found or built.
)

echo.
pause
