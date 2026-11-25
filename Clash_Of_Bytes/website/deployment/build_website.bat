@echo off
setlocal enabledelayedexpansion

echo =======================================================================
echo   CLASH OF BYTES: FULL-STACK PUBLIC WEBSITE BUILDER
echo   Components: Database Migrations, React Frontend, Java REST Backend
echo =======================================================================

cd /d "%~dp0..\.."

echo [*] Step 1: Compiling Java REST Backend...
if not exist "website\backend\bin" mkdir "website\backend\bin"
"C:\Program Files\Java\latest\jdk-27\bin\javac.exe" -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" -d "website/backend/bin" "website/backend/src/main/java/com/clashofbytes/StandaloneWebServer.java"
if %errorlevel% neq 0 (
    echo [ERROR] Backend compilation failed!
    exit /b 1
)
echo [SUCCESS] Java Backend compiled into website\backend\bin\

echo [*] Step 2: Running Automated Backend Verification Test Suite...
"C:\Program Files\Java\latest\jdk-27\bin\javac.exe" -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" -d "website/backend/bin" "website/backend/src/test/java/com/clashofbytes/BackendTestSuite.java"
"C:\Program Files\Java\latest\jdk-27\bin\java.exe" -cp "website/backend/bin;4_Object_Oriented_Programming_Java/lib/*" com.clashofbytes.BackendTestSuite
if %errorlevel% neq 0 (
    echo [ERROR] Backend test suite failed!
    exit /b 1
)

echo [*] Step 3: Checking Database Migration Script...
python "website/database/migrate_sqlite_to_postgres.py"
echo [SUCCESS] Database migrations generated!

echo [*] Step 4: Compiling React Frontend with Vite...
cd website\frontend
call npm install
call npm run build
cd ..\..
echo [SUCCESS] React Frontend compiled into website\frontend\dist\

echo =======================================================================
echo  WEBSITE BUILD SUCCEEDED! Run 'website\deployment\run_website.bat' to launch!
echo =======================================================================
pause
