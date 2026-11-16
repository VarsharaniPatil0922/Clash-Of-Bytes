@echo off
cd /d "%~dp0"

echo =======================================================================
echo   CLASH OF BYTES (PRACTICALS 1 TO 4 / TASK 4 COMPLETE) COMPILER
echo   Subjects: Computer Graphics, DSA, COA and OOP Java
echo =======================================================================
echo.

:: Auto-detect GCC toolchain
if exist "C:\msys64\mingw64\bin\g++.exe" (
    set "PATH=C:\msys64\mingw64\bin;C:\msys64\ucrt64\bin;%PATH%"
) else if exist "C:\msys64\ucrt64\bin\g++.exe" (
    set "PATH=C:\msys64\ucrt64\bin;C:\msys64\mingw64\bin;%PATH%"
)

:: Auto-detect Javac compiler
if exist "C:\Program Files\Java\latest\jdk-27\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\latest\jdk-27\bin\javac.exe"
) else if exist "jdk\bin\javac.exe" (
    set "JAVAC_CMD=jdk\bin\javac.exe"
) else if exist "..\retro_fighter_2d\jdk\bin\javac.exe" (
    set "JAVAC_CMD=..\retro_fighter_2d\jdk\bin\javac.exe"
) else (
    where javac >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        set "JAVAC_CMD=javac"
    ) else (
        set "JAVAC_CMD="
    )
)

echo [*] Compiling C++ Real-Time Arcade Engine (clash_of_bytes.exe)...

g++ -std=c++17 -O2 -Wall -mwindows -static -static-libgcc -static-libstdc++ ^
    1_Computer_Graphics/main.cpp ^
    1_Computer_Graphics/OpenGL_Window.cpp ^
    1_Computer_Graphics/CO1_Prac01_OpenGL_Gouraud_Renderer.cpp ^
    1_Computer_Graphics/Fighter.cpp ^
    1_Computer_Graphics/Warrior.cpp ^
    1_Computer_Graphics/Ninja.cpp ^
    1_Computer_Graphics/CyberBrawler.cpp ^
    1_Computer_Graphics/Paladin.cpp ^
    1_Computer_Graphics/Berserker.cpp ^
    1_Computer_Graphics/Gladiator.cpp ^
    1_Computer_Graphics/GameMatch.cpp ^
    1_Computer_Graphics/InputManager.cpp ^
    -I1_Computer_Graphics -I2_Programming_Laboratory_DSA -I3_Computer_Organization_Architecture ^
    -o clash_of_bytes.exe ^
    -lopengl32 -lgdi32 -luser32

set CXX_ERR=%ERRORLEVEL%

if %CXX_ERR% EQU 0 (
    echo [SUCCESS] clash_of_bytes.exe compiled successfully!
) else (
    echo [ERROR] C++ Compilation failed with error code %CXX_ERR%!
    pause
    exit /b %CXX_ERR%
)

:: Compile DSA Practicals Test Suite
echo.
echo [*] Compiling Programming Lab (DSA) Practicals 01 to 09 and Task 4 Suite...
g++ -std=c++17 -O2 -Wall 2_Programming_Laboratory_DSA/CO1_to_CO3_DSAManager.cpp -o dsa_practicals.exe
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] dsa_practicals.exe compiled successfully!
) else (
    echo [WARNING] DSA suite compilation encountered an issue.
)

:: Compile COA Practicals Test Suite
echo.
echo [*] Compiling Computer Organization and Architecture (COA) Practicals 01 to 04 and Task 4 Integrated Suite...
g++ -std=c++17 -O2 -Wall 3_Computer_Organization_Architecture/CO1_to_CO4_COAManager.cpp -o coa_practicals.exe
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] coa_practicals.exe compiled successfully!
) else (
    echo [WARNING] COA suite compilation encountered an issue.
)

:: Compile Java Module
echo.
echo [*] Compiling Java OOP Module (4_Object_Oriented_Programming_Java)...
if not exist "4_Object_Oriented_Programming_Java\bin" mkdir 4_Object_Oriented_Programming_Java\bin

if not defined JAVAC_CMD (
    echo [NOTE] Javac compiler not found in PATH; using existing compiled classes in bin\
    goto :BUILD_DONE
)

powershell -NoProfile -Command "Get-ChildItem -Path 4_Object_Oriented_Programming_Java -Recurse -Filter *.java | Resolve-Path -Relative | Out-File -Encoding ascii java_sources.txt" >nul 2>&1
"%JAVAC_CMD%" -cp "4_Object_Oriented_Programming_Java/lib/*;4_Object_Oriented_Programming_Java/bin" -d 4_Object_Oriented_Programming_Java/bin @java_sources.txt
if exist java_sources.txt del java_sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Java classes compiled into 4_Object_Oriented_Programming_Java\bin\
) else (
    echo [WARNING] Java compilation failed.
)

:BUILD_DONE

echo.
echo =======================================================================
echo  BUILD COMPLETE! Run 'run_game.bat' to start playing the game!
echo =======================================================================
