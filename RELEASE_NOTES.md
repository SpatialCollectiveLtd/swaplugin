# DPW Mapper Support Plugin - Release v1.0.0

## Release Date
November 24, 2025

## Overview
This is the initial framework release of the DPW Mapper Support Plugin for JOSM. This release establishes the project structure, build system, and comprehensive documentation.

## Current Status

### ✅ Completed
- **Project Structure**: Complete Maven/Gradle build system configured
- **Documentation**: Comprehensive documentation suite (11+ files, 3000+ lines)
  - README, QUICKSTART, USER_GUIDE, TECHNICAL, BUILD guides
  - TESTING, CONTRIBUTING, CHANGELOG documentation
  - Complete API and architecture documentation
- **Build System**: Gradle 8.11.1 with Java 17 compatibility
- **Version Control**: Git repository with proper .gitignore and LICENSE (MIT)
- **GitHub Repository**: https://github.com/SpatialCollectiveLtd/swaplugin.git
- **CI/CD**: GitHub Actions workflow configured
- **Compilable JAR**: Successfully builds DPWMapper-1.0.0.jar

### ⏳ In Progress (Planned for v1.1.0)
- **Full JOSM Integration**: The complete plugin implementation with JOSM API integration
  - Auto-Hide Filter: Automatically hide existing OSM data on download
  - Merge & Fix Action: Spatial matching and geometry merging
  - Full event listener implementation
  
The source code for these features has been developed and documented, but requires:
- Proper JOSM plugin dependencies and API compatibility verification
- Integration testing with JOSM application
- User testing in production environment

## Installation

### Build from Source
```bash
# Requires Java 17
./gradlew clean build jar
```

The JAR file will be created at: `build/libs/DPWMapper-1.0.0.jar`

### Manual Installation
1. Download `DPWMapper-1.0.0.jar` from the releases page
2. Copy to JOSM plugins directory:
   - **Linux**: `~/.local/share/JOSM/plugins/`
   - **Windows**: `%APPDATA%\JOSM\plugins\`
   - **macOS**: `~/Library/JOSM/plugins/`
3. Restart JOSM

## What's in This Release

### Framework Components
- Plugin entry point class: `DPWMapperPlugin`
- Build configuration for JAR packaging
- Plugin metadata in `dpwmapper.properties`
- Manifest configuration for JOSM compatibility

### Documentation
Extensive documentation covering:
- User guides and quickstart
- Technical architecture and design
- Build and testing procedures
- Contributing guidelines
- API documentation for planned features

## Known Limitations

This v1.0.0 release is a framework release. The plugin will load in JOSM but does not yet provide the full auto-hide and merge functionality described in the documentation. These features are fully designed and documented, awaiting JOSM API integration testing in v1.1.0.

## Development Roadmap

### v1.1.0 (Next Release)
- Full JOSM API integration
- Auto-hide filter implementation
- Merge & Fix action implementation
- End-to-end testing with JOSM
- User acceptance testing

### v1.2.0 (Future)
- Performance optimizations
- Additional configuration options
- Enhanced conflict resolution
- Batch processing capabilities

## Technical Details

- **Java Version**: Compiled with Java 17, targeting Java 8 bytecode
- **JOSM Compatibility**: Designed for JOSM 18729+
- **Build Tool**: Gradle 8.11.1
- **License**: MIT License

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Support

- **GitHub Issues**: https://github.com/SpatialCollectiveLtd/swaplugin/issues
- **Documentation**: See `/docs` directory
- **Technical Guide**: [TECHNICAL.md](TECHNICAL.md)

## Acknowledgments

- Developed by Spatial Collective Ltd
- Built for youth mapper workflows
- Designed for OpenStreetMap community

---

**Note**: This is a framework release (v1.0.0). Full functionality will be available in v1.1.0 after JOSM integration testing is complete.
