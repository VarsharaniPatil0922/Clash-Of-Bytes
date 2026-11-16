@echo off
setlocal enabledelayedexpansion

echo =======================================================================
echo   CLASH OF BYTES: FULL-STACK PUBLIC WEBSITE LAUNCHER
echo =======================================================================

cd /d "%~dp0..\.."

echo [*] Launching Java REST Backend on port 8080...
start "ClashOfBytes_Backend" "C:\Program Files\Java\latest\jdk-27\bin\java.exe" -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" com.clashofbytes.StandaloneWebServer 8080

echo [*] Opening Web Arena Client in default browser...
timeout /t 2 /nobreak >nul
start "" "http://localhost:8080/index.html"

echo =======================================================================
echo  Server is running at: http://localhost:8080/
echo  REST API Endpoints:   http://localhost:8080/api/
echo =======================================================================
pause
