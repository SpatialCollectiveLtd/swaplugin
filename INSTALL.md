# Installation Guide

## Installing the DPW Mapper Plugin in JOSM

### Method 1: Automated Installation (Recommended)

1. Build the plugin:
   ```bash
   ./gradlew installPlugin
   ```

2. Restart JOSM

3. Go to **Edit → Preferences → Plugins**

4. Search for "DPW Mapper" and enable it

5. Click OK and restart JOSM if prompted

### Method 2: Manual Installation

1. Build the JAR file:
   ```bash
   ./gradlew jar
   ```

2. Locate the built JAR file:
   - It will be in `build/libs/DPWMapper-1.0.0.jar`

3. Copy the JAR file to your JOSM plugins directory:

   **Windows:**
   ```
   %APPDATA%\JOSM\plugins\
   ```
   (Usually: `C:\Users\YourName\AppData\Roaming\JOSM\plugins\`)

   **macOS:**
   ```
   ~/Library/JOSM/plugins/
   ```

   **Linux:**
   ```
   ~/.local/share/JOSM/plugins/
   ```

4. Restart JOSM

5. Go to **Edit → Preferences → Plugins**

6. Find "DPW Mapper Support Plugin" in the list and check the box

7. Click OK and restart JOSM

### Verification

After installation, you should see:
- A new menu item under **Tools → Merge & Fix**
- The plugin will automatically activate when you download OSM data
- A notification "Clean Slate Active" will appear when data is hidden

### Troubleshooting

**Plugin doesn't appear in the list:**
- Ensure the JAR file is in the correct plugins directory
- Check JOSM version compatibility (requires JOSM 18729+)
- Check the JOSM console (View → Toggle Console) for error messages

**Filter doesn't activate automatically:**
- Ensure you're downloading data via Remote Control (e.g., from Tasking Manager)
- Check that the Filter dialog is visible (Windows → Filter)

**Merge & Fix button doesn't work:**
- Ensure you have an active data layer
- Make sure there are both new (unuploaded) and old (downloaded) buildings
- Check that buildings have the `building=*` tag

## Building from Source

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Gradle (or use the included wrapper: `./gradlew`)

### Build Commands

**Build the JAR:**
```bash
./gradlew jar
```

**Build and install:**
```bash
./gradlew installPlugin
```

**Clean build:**
```bash
./gradlew clean build
```

## Uninstalling

1. Go to **Edit → Preferences → Plugins**
2. Uncheck "DPW Mapper Support Plugin"
3. Click OK
4. Optionally delete the JAR file from the plugins directory
