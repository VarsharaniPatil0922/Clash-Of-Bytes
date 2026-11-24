param([string]$FilePath)

if (-not $FilePath) {
    Write-Error "Please specify a Java file to run."
    exit 1
}

$fileItem = Get-Item $FilePath
$dir = $fileItem.Directory
$proj = $dir
while ($proj -and -not (Test-Path (Join-Path $proj.FullName "jdk\bin\java.exe"))) {
    $proj = $proj.Parent
}

if (-not $proj) {
    $proj = Get-Item "C:\Users\Atharva\Downloads\clash_of_bytes_milestone1_task3\clash_of_bytes_milestone1_latest (2)\clash_of_bytes_milestone1_latest"
}

$javaExe = Join-Path $proj.FullName "jdk\bin\java.exe"
if (-not (Test-Path $javaExe)) {
    $javaExe = "java"
}

$javacExe = Join-Path $proj.FullName "jdk\bin\javac.exe"
if (-not (Test-Path $javacExe)) {
    $javacExe = "javac"
}

$binDir = Join-Path $proj.FullName "4_Object_Oriented_Programming_Java\bin"
$libDir = Join-Path $proj.FullName "4_Object_Oriented_Programming_Java\lib\*"
$cp = "$binDir;$libDir"

$pkgLine = Get-Content $FilePath | Where-Object { $_ -match '^\s*package\s+([^;]+);' } | Select-Object -First 1
$pkg = if ($pkgLine) { ($pkgLine -replace '^\s*package\s+([^;]+);.*', '$1').Trim() } else { "" }
$className = $fileItem.BaseName
$fullClass = if ($pkg) { "$pkg.$className" } else { $className }

Write-Host "[*] Compiling $className using $javacExe..." -ForegroundColor Cyan
& $javacExe -cp $cp -d $binDir $FilePath

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed."
    exit $LASTEXITCODE
}

Write-Host "[*] Executing $fullClass..." -ForegroundColor Green
& $javaExe -cp $cp $fullClass
