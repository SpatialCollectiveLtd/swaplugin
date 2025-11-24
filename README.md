# 🗺️ DPW Mapper Support Plugin

**The Clean Slate Tool for JOSM**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JOSM](https://img.shields.io/badge/JOSM-18729%2B-green.svg)](https://josm.openstreetmap.de/)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.java.com/)

A powerful JOSM plugin that automates the "clean slate" remapping workflow for youth mappers in the Digital Public Works program, eliminating data duplication while preserving OpenStreetMap history.

---

## 🎯 Quick Links

📖 **[Documentation Index](DOCS_INDEX.md)** | 🚀 **[Quick Start](QUICKSTART.md)** | 📚 **[User Guide](USER_GUIDE.md)** | 🔧 **[Technical Docs](TECHNICAL.md)**

---

## Overview

This plugin solves the duplicate data problem during remapping projects by:
1. **Auto-hiding existing OSM data** when downloaded (provides a visual "clean slate")
2. Allowing mappers to trace buildings on blank imagery
3. **Automatically merging new geometries** onto old IDs to preserve OpenStreetMap history

## Features

### 1. Auto-Hide Filter (Clean Slate View)
- Automatically applies when OSM data is downloaded via Tasking Manager
- Hides all existing data (id > 0) using filter: `id:1-`
- Keeps newly created objects (id < 0) visible
- Provides clean imagery-only view for accurate tracing

### 2. Merge & Fix Action
- Single-button workflow to merge new drawings with existing data
- Spatial matching algorithm (50% overlap threshold)
- Transfers geometry from new buildings to old building IDs
- Preserves OSM history, tags, and metadata
- Handles conflicts intelligently (selects for manual review)
- Entire operation is undoable (Ctrl+Z)

## Installation

### From Source
1. Clone this repository:
   ```bash
   git clone https://github.com/SpatialCollectiveLtd/swaplugin.git
   cd swaplugin
   ```

2. Build the plugin:
   ```bash
   ./gradlew jar
   ```

3. Install to JOSM:
   ```bash
   ./gradlew installPlugin
   ```
   Or manually copy `build/libs/DPWMapper-1.0.0.jar` to:
   - **Windows:** `%APPDATA%\JOSM\plugins\`
   - **macOS:** `~/Library/JOSM/plugins/`
   - **Linux:** `~/.local/share/JOSM/plugins/`

4. Restart JOSM and enable the plugin in Preferences → Plugins

## Usage

### The Happy Path Workflow

1. **Start Task:** Click "Start Editor" in HOT Tasking Manager
   - JOSM downloads OSM data for the task area
   - Plugin automatically hides existing data
   - You see only satellite imagery (clean slate)

2. **Map Buildings:** Trace all visible buildings
   - Your new drawings appear clearly on imagery
   - Old data remains hidden

3. **Merge:** Click the "Merge & Fix" button (or press `Ctrl+Alt+M`)
   - Plugin finds overlaps between new and old buildings
   - Transfers geometry from new → old IDs
   - Deletes temporary new drawings
   - Preserves all tags and history

4. **Review:** Filter is disabled automatically
   - See final result (old IDs with new shapes)
   - Conflicts are selected for manual review

5. **Upload:** Use standard JOSM upload

## Technical Details

### Algorithm

The merge algorithm:
1. Separates ways into new (id < 0) and old (id > 0)
2. For each new building:
   - Checks bounding box intersection (fast)
   - Calculates precise overlap area
   - If overlap > 50% of old building area → match found
3. Creates commands to:
   - Update old way nodes with new way geometry
   - Delete temporary new way
   - Preserve old way tags
4. Executes as single undo/redo transaction

### Safety Features

- **Tag Preservation:** Old building tags remain unchanged
- **Conflict Detection:** Multiple new buildings matching one old building are flagged
- **Undo Support:** Entire merge is one undo step
- **Manual Review:** Conflicts are auto-selected for mapper review

## Development

### Requirements
- Java 8 or higher
- Gradle
- JOSM (latest stable version recommended)

### Building
```bash
./gradlew build
```

### Testing
Install the plugin in JOSM test mode:
```bash
./gradlew installPlugin
josm
```

## Architecture

```
DPWMapperPlugin (main)
├── AutoHideListener (detects downloads, applies filter)
└── MergeAndFixAction (geometry matching & merging)
```

## References

This plugin's geometry swapping functionality is inspired by the [utilsplugin2](https://github.com/JOSM/utilsplugin2) ReplaceGeometry tool.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

**Spatial Collective Ltd**

## Contributing

Contributions welcome! Please submit issues and pull requests on GitHub.

## Support

For questions or issues:
- GitHub Issues: https://github.com/SpatialCollectiveLtd/swaplugin/issues
- Contact: [Your contact information]
