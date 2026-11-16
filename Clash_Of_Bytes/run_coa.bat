@echo off
cd /d "%~dp0"

echo =======================================================================
echo   COMPUTER ORGANIZATION AND ARCHITECTURE (COA) - PRACTICALS RUNNER
echo   Course Code: 2310221L - Practicals 01 to 04 and Task 3 Suite
echo =======================================================================
echo.

if exist "C:\msys64\mingw64\bin\g++.exe" set "PATH=C:\msys64\mingw64\bin;C:\msys64\ucrt64\bin;%PATH%"
if exist "C:\msys64\ucrt64\bin\g++.exe" set "PATH=C:\msys64\ucrt64\bin;C:\msys64\mingw64\bin;%PATH%"

echo [*] Building COA practicals test driver...
g++ -std=c++17 -O2 -Wall 3_Computer_Organization_Architecture/CO1_to_CO4_COAManager.cpp -o coa_practicals.exe


if exist "coa_practicals.exe" (
    .\coa_practicals.exe %*
) else (
    echo [ERROR] coa_practicals.exe could not be found or built.
)

echo.
pause
