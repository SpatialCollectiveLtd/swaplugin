# Simple build script for DPW Mapper Plugin
Write-Host "Building DPW Mapper Plugin..." -ForegroundColor Green

# Create build directories
$buildDir = "build-manual"
$classesDir = "$buildDir/classes"
$libsDir = "$buildDir/libs"

Remove-Item -Recurse -Force $buildDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $libsDir | Out-Null

# Download JOSM if not present
$josmJar = "$buildDir/josm.jar"
if (-not (Test-Path $josmJar)) {
    Write-Host "Downloading JOSM..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://josm.openstreetmap.de/download/josm.jar" -OutFile $josmJar
}

# Compile Java files
Write-Host "Compiling Java files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse
$javaFilePaths = $javaFiles | ForEach-Object { $_.FullName }

javac -source 8 -target 8 -cp $josmJar -d $classesDir $javaFilePaths

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Copy resources
Write-Host "Copying resources..." -ForegroundColor Yellow
Copy-Item -Path "src/main/resources/*" -Destination $classesDir -Recurse -Force -ErrorAction SilentlyContinue

# Create manifest
$manifestContent = @"
Manifest-Version: 1.0
Plugin-Class: org.openstreetmap.josm.plugins.dpwmapper.DPWMapperPlugin
Plugin-Description: Automates clean slate mapping workflow with geometry merging
Plugin-Version: 1.0.0
Plugin-Mainversion: 18729
Plugin-Date: 2025-11-24
Plugin-Author: Spatial Collective Ltd

"@

$manifestFile = "$buildDir/MANIFEST.MF"
$manifestContent | Out-File -FilePath $manifestFile -Encoding ASCII

# Create JAR
Write-Host "Creating JAR file..." -ForegroundColor Yellow
$jarFile = "$libsDir/DPWMapper.jar"

Push-Location $classesDir
jar cfm $jarFile "../MANIFEST.MF" *
Pop-Location

if (Test-Path $jarFile) {
    Write-Host "`nBuild successful!" -ForegroundColor Green
    Write-Host "JAR file created: $jarFile" -ForegroundColor Green
    
    $jarSize = (Get-Item $jarFile).Length
    Write-Host "Size: $($jarSize / 1KB) KB" -ForegroundColor Cyan
    
    # Optionally install to JOSM
    $josmPluginsDir = "$env:APPDATA\JOSM\plugins"
    if (Test-Path $josmPluginsDir) {
        Write-Host "`nDo you want to install to JOSM? (Y/N)" -ForegroundColor Yellow
        $response = Read-Host
        if ($response -eq 'Y' -or $response -eq 'y') {
            Copy-Item $jarFile "$josmPluginsDir\DPWMapper.jar" -Force
            Write-Host "Installed to: $josmPluginsDir\DPWMapper.jar" -ForegroundColor Green
            Write-Host "Restart JOSM and enable the plugin in Preferences > Plugins" -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}
