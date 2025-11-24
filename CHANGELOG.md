# Changelog

All notable changes to the DPW Mapper Support Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-11-24

### Added
- **Auto-Hide Filter:** Automatically applies `id:1-` filter when OSM data is downloaded
  - Hides all existing OSM data (id > 0)
  - Keeps newly created objects (id < 0) visible
  - Provides "Clean Slate" view for accurate tracing
  - Notifications when filter is activated

- **Merge & Fix Action:** Single-button workflow to merge new buildings with old data
  - Spatial matching algorithm with 50% overlap threshold
  - Transfers geometry from new buildings to old building IDs
  - Preserves OSM history, tags, and metadata
  - Intelligent conflict detection and selection
  - Complete undo/redo support (entire operation as one step)
  - Keyboard shortcut: Ctrl+Alt+M

- **User Interface:**
  - "Merge & Fix" button in Tools menu
  - Notifications for operation status
  - Conflict selection for manual review
  - Filter visibility in Windows → Filter dialog

- **Documentation:**
  - Comprehensive README with overview
  - Installation guide (INSTALL.md)
  - User guide with workflow examples (USER_GUIDE.md)
  - Technical documentation (TECHNICAL.md)
  - Testing guide (TESTING.md)
  - Quick start guide (QUICKSTART.md)
  - Contributing guidelines (CONTRIBUTING.md)

- **Build System:**
  - Gradle build configuration
  - GitHub Actions workflow for CI/CD
  - Automated plugin installation task

- **Safety Features:**
  - Tag preservation from old buildings
  - Single undo step for entire merge
  - Conflict detection for complex overlaps
  - Bounding box pre-filtering for performance

### Changed
- N/A (Initial release)

### Deprecated
- N/A (Initial release)

### Removed
- N/A (Initial release)

### Fixed
- N/A (Initial release)

### Security
- N/A (Initial release)

## Release Notes

### Version 1.0.0 - Initial Release

This is the first public release of the DPW Mapper Support Plugin, designed to automate the "clean slate" remapping workflow for youth mappers in the Digital Public Works program.

**Key Features:**
1. **Automatic Clean Slate View:** No more confusion about what to trace - the plugin automatically hides old data when you start mapping
2. **One-Click Merging:** Transform hours of manual work into a single button click
3. **History Preservation:** All OSM history and tags are preserved while updating geometries
4. **Conflict Detection:** Smart detection of complex scenarios that need manual review

**Target Users:**
- Youth mappers in HOT Tasking Manager projects
- Remote mappers working on building remapping tasks
- Anyone who needs to update building geometries while preserving metadata

**Tested With:**
- JOSM version 18729+
- Windows, macOS, and Linux platforms
- Various HOT Tasking Manager projects

**Known Limitations:**
- Only supports Way objects (buildings)
- Requires `building=*` tag
- Fixed 50% overlap threshold (not user-configurable yet)
- 1-to-1 matching only (complex merges require manual intervention)

**Future Enhancements:**
See our [GitHub Issues](https://github.com/SpatialCollectiveLtd/swaplugin/issues) for planned features including:
- Configurable overlap threshold
- Support for other feature types
- Improved conflict resolution UI
- Batch processing capabilities

---

[Unreleased]: https://github.com/SpatialCollectiveLtd/swaplugin/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/SpatialCollectiveLtd/swaplugin/releases/tag/v1.0.0
