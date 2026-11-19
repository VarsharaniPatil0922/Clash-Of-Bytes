@echo off
cd /d "%~dp0"

echo =======================================================================
echo   COMPUTER GRAPHICS LABORATORY - PRACTICALS 01 TO 06 & TASK 1-4 RUNNER
echo   Course Code: 2310218L | Focus: Spinning Cube & Line Clipping
echo =======================================================================
echo.

if exist "C:\msys64\mingw64\bin\g++.exe" set "PATH=C:\msys64\mingw64\bin;C:\msys64\ucrt64\bin;%PATH%"
if exist "C:\msys64\ucrt64\bin\g++.exe" set "PATH=C:\msys64\ucrt64\bin;C:\msys64\mingw64\bin;%PATH%"

if not exist "cg_practicals.exe" (
    echo [*] Compiling Computer Graphics practicals suite...
    g++ -std=c++17 -O2 1_Computer_Graphics/cg_practicals_runner.cpp -I1_Computer_Graphics -o cg_practicals.exe -lopengl32 -lgdi32
)

if exist "cg_practicals.exe" (
    .\cg_practicals.exe %*
) else (
    echo [ERROR] cg_practicals.exe could not be found or built.
)

echo.
pause
