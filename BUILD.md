# Build and Deployment Guide

## Prerequisites Check

Before building, ensure you have:
- ✅ Java Development Kit (JDK) 8 or higher
- ✅ Git (for version control)
- ✅ JOSM installed (for testing)

### Verify Prerequisites (Windows)

```powershell
# Check Java version
java -version

# Should show: java version "1.8.0_xxx" or higher

# Check if JOSM is installed
Test-Path "$env:APPDATA\JOSM"

# Should return: True
```

## Build Process

### Step 1: Clean Build

```powershell
# Clean previous builds
./gradlew.bat clean

# Expected output:
# BUILD SUCCESSFUL
```

### Step 2: Build JAR

```powershell
# Build the plugin JAR
./gradlew.bat jar

# Expected output:
# BUILD SUCCESSFUL
# JAR created at: build\libs\DPWMapper-1.0.0.jar
```

### Step 3: Verify JAR

```powershell
# Check if JAR was created
Test-Path "build\libs\DPWMapper-1.0.0.jar"

# Should return: True

# Check JAR size (should be > 10KB)
(Get-Item "build\libs\DPWMapper-1.0.0.jar").Length
```

## Installation Options

### Option 1: Automated Install

```powershell
# Install directly to JOSM plugins directory
./gradlew.bat installPlugin

# Expected output:
# BUILD SUCCESSFUL
# Plugin installed to: [JOSM plugins directory]
```

### Option 2: Manual Install

```powershell
# Create plugins directory if it doesn't exist
New-Item -ItemType Directory -Force -Path "$env:APPDATA\JOSM\plugins"

# Copy JAR file
Copy-Item "build\libs\DPWMapper-1.0.0.jar" "$env:APPDATA\JOSM\plugins\"

# Verify installation
Test-Path "$env:APPDATA\JOSM\plugins\DPWMapper-1.0.0.jar"
```

## Testing the Plugin

### Quick Verification Test

```powershell
# 1. Start JOSM
Start-Process "josm"

# 2. Enable the plugin:
#    - Go to Edit → Preferences → Plugins
#    - Search for "DPW Mapper"
#    - Check the box
#    - Click OK and restart JOSM

# 3. Verify installation:
#    - Check Tools menu for "Merge & Fix"
#    - Download some OSM data
#    - Verify filter "id:1-" appears in Windows → Filter
```

### Full Integration Test

1. **Download Test Data**
   - Go to File → Download Data
   - Select area with buildings
   - Click Download

2. **Verify Auto-Hide**
   - Check for notification: "Clean Slate Active"
   - Verify map appears empty (only imagery)
   - Check Windows → Filter shows "id:1-" enabled

3. **Test Merge**
   - Draw a new building over hidden old data
   - Tag with `building=yes`
   - Click Tools → Merge & Fix
   - Verify notification: "Merged X buildings"

4. **Verify Result**
   - Check old building has new geometry
   - Verify tags preserved
   - Test undo (Ctrl+Z)

## Deployment to GitHub

### First Time Setup

```powershell
# Initialize git repository
git init

# Add remote
git remote add origin https://github.com/SpatialCollectiveLtd/swaplugin.git

# Add all files
git add .

# Commit
git commit -m "Initial commit - DPW Mapper Plugin v1.0.0"

# Push to GitHub
git push -u origin main
```

### Creating a Release

```powershell
# Tag the release
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag
git push origin v1.0.0

# GitHub Actions will automatically:
# - Build the JAR
# - Create a GitHub Release
# - Attach the JAR as a release asset
```

## CI/CD Pipeline

The project uses GitHub Actions for automated builds.

### Triggers

- Push to `main` or `develop` branch
- Pull requests to `main`
- Release creation

### Build Steps

1. Checkout code
2. Set up JDK 8
3. Grant execute permission to gradlew
4. Build with Gradle
5. Create JAR
6. Upload artifact
7. (On release) Attach to GitHub Release

### Viewing Build Results

- Go to GitHub repository
- Click "Actions" tab
- View build status and logs

## Distribution

### GitHub Releases

1. Users download `DPWMapper-1.0.0.jar` from Releases page
2. Copy to JOSM plugins directory
3. Enable in JOSM preferences

### JOSM Plugin List (Future)

To submit to official JOSM plugin list:

1. Build stable version
2. Test thoroughly
3. Submit to JOSM plugin repository
4. Follow: https://josm.openstreetmap.de/wiki/PluginSubmission

## Troubleshooting Build Issues

### Issue: "JAVA_HOME not set"

```powershell
# Set JAVA_HOME environment variable
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_xxx"

# Verify
echo $env:JAVA_HOME
```

### Issue: "gradlew command not found"

```powershell
# Make sure you're in the project directory
cd c:\Users\TECH\Desktop\swaplugin

# Use Windows batch file
./gradlew.bat build
```

### Issue: "Permission denied"

```powershell
# On Unix/Linux, make gradlew executable
chmod +x gradlew

# On Windows, use .bat version
./gradlew.bat build
```

### Issue: "Build fails with dependency errors"

```powershell
# Clear Gradle cache
./gradlew.bat clean --refresh-dependencies

# Rebuild
./gradlew.bat build
```

### Issue: "JAR created but JOSM doesn't recognize it"

**Check:**
- JAR contains proper manifest (Plugin-Class, etc.)
- JAR is in correct JOSM plugins directory
- JOSM version is 18729 or higher
- Plugin is enabled in preferences

**Verify manifest:**
```powershell
# Extract and view manifest
jar xf build\libs\DPWMapper-1.0.0.jar META-INF\MANIFEST.MF
Get-Content META-INF\MANIFEST.MF
```

## Version Updates

### Updating Version Number

Edit three files:

1. **build.gradle**
```gradle
version = '1.1.0'  // Update here
```

2. **src/main/resources/dpwmapper.properties**
```properties
plugin.version=1.1.0  # Update here
```

3. **CHANGELOG.md**
```markdown
## [1.1.0] - 2025-XX-XX
### Added
- New feature description
```

### Build and Release

```powershell
# Clean and build
./gradlew.bat clean build

# Commit changes
git add .
git commit -m "chore: bump version to 1.1.0"

# Tag and push
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin main --tags
```

## Quality Checks

Before releasing:

- [ ] Build succeeds without errors
- [ ] Plugin loads in JOSM
- [ ] All features work as expected
- [ ] No console errors
- [ ] Documentation updated
- [ ] Changelog updated
- [ ] Version numbers consistent
- [ ] Tests pass
- [ ] No regressions

## Support

For build issues:
- Check [INSTALL.md](INSTALL.md)
- Review [CONTRIBUTING.md](CONTRIBUTING.md)
- Open GitHub Issue with build logs

---

**Ready to build? Start with:**
```powershell
./gradlew.bat clean build installPlugin
```
