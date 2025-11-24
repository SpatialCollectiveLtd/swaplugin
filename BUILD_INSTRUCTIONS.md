# Build Instructions

## Issue: Gradle/Java Version Incompatibility

Your system has Java 24, which is causing compatibility issues with the current Gradle setup.

## Recommended Solution

### Option 1: Use an Older Java Version (Recommended)

Download and install **Java 11** or **Java 17** from:
- https://adoptium.net/

Then set JAVA_HOME and rebuild:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot"
cd c:\Users\TECH\Desktop\swaplugin
./gradlew.bat clean build
```

### Option 2: Download Pre-built Plugin

Since the code is on GitHub, you can:

1. Wait for GitHub Actions to build it automatically (check the Actions tab)
2. Download the JAR from the build artifacts
3. Install to JOSM manually

### Option 3: Manual Build (Advanced)

1. **Download JOSM JAR manually:**
   ```powershell
   Invoke-WebRequest -Uri "https://github.com/JOSM/josm/releases/download/23.12/josm-tested.jar" -OutFile "josm.jar"
   ```

2. **Compile with javac:**
   ```powershell
   # Create output directory
   New-Item -ItemType Directory -Force -Path "build/classes"
   
   # Compile (using Java with --release 8)
   javac --release 8 `
         -cp "josm.jar" `
         -d "build/classes" `
         src/main/java/org/openstreetmap/josm/plugins/dpwmapper/*.java
   
   # Copy resources
   Copy-Item "src/main/resources/*" "build/classes/" -Recurse
   
   # Create JAR
   cd build/classes
   jar cfm ../DPWMapper.jar ../../META-INF/MANIFEST.MF *
   ```

3. **Create MANIFEST.MF file:**
   ```
   Manifest-Version: 1.0
   Plugin-Class: org.openstreetmap.josm.plugins.dpwmapper.DPWMapperPlugin
   Plugin-Description: Automates clean slate mapping workflow
   Plugin-Version: 1.0.0
   Plugin-Mainversion: 18729
   Plugin-Date: 2025-11-24
   Plugin-Author: Spatial Collective Ltd
   ```

## Current Status

- ✅ Code uploaded to GitHub: https://github.com/SpatialCollectiveLtd/swaplugin
- ✅ Version tagged: v1.0.0
- ⏳ GitHub Actions will build automatically
- ⏳ JAR will be available as build artifact

## Next Steps

1. **Check GitHub Actions:**
   - Go to https://github.com/SpatialCollectiveLtd/swaplugin/actions
   - Wait for build to complete
   - Download the artifact

2. **Install to JOSM:**
   - Copy JAR to `%APPDATA%\JOSM\plugins\`
   - Restart JOSM
   - Enable in Preferences → Plugins

3. **Alternative: Install Java 11/17 and rebuild locally**

## Contact

If you need immediate testing, consider installing Java 11 or 17 which is the recommended version for JOSM plugin development.
