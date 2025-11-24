# Quick Start Guide

## 5-Minute Setup

### 1. Build the Plugin (1 minute)
```bash
cd swaplugin
./gradlew.bat jar
```

### 2. Install to JOSM (1 minute)
**Windows:**
```powershell
copy build\libs\DPWMapper-1.0.0.jar %APPDATA%\JOSM\plugins\
```

**Or use automated install:**
```bash
./gradlew.bat installPlugin
```

### 3. Enable in JOSM (1 minute)
1. Start JOSM
2. Edit → Preferences → Plugins
3. Find "DPW Mapper Support Plugin"
4. Check the box
5. Click OK
6. Restart JOSM

### 4. Test It! (2 minutes)
1. Go to HOT Tasking Manager: https://tasks.hotosm.org/
2. Find a project and select a task
3. Click "Edit with JOSM"
4. **Watch:** Notification appears "Clean Slate Active"
5. **See:** Only imagery visible (old buildings hidden)
6. Draw a few test buildings
7. Click Tools → Merge & Fix (or Ctrl+Alt+M)
8. **Result:** Buildings merged successfully!

## First Real Mapping Session

### Before You Start
✅ JOSM installed and configured  
✅ Plugin installed and enabled  
✅ Tasking Manager account created  
✅ Imagery aligned and ready

### The Workflow
1. **Start Task** → Click "Edit with JOSM" in Tasking Manager
2. **See Clean Slate** → Notice "Clean Slate Active" notification
3. **Trace Buildings** → Draw all visible buildings on imagery
4. **Add Tags** → Tag buildings (at minimum: `building=yes`)
5. **Merge** → Click "Merge & Fix" button
6. **Review** → Check a few buildings look correct
7. **Upload** → Standard JOSM upload
8. **Done!** → Mark task complete in Tasking Manager

### Common First-Time Questions

**Q: I don't see the "Clean Slate Active" notification**  
A: Check Windows → Filter for "id:1-" filter and ensure it's enabled

**Q: Can I see the old data before merging?**  
A: Yes! Temporarily disable the filter in Windows → Filter

**Q: What if I make a mistake?**  
A: Press Ctrl+Z to undo the entire merge operation

**Q: Do I need to tag the buildings?**  
A: Yes! At minimum add `building=yes`, better to add specific types

**Q: What happens to the old building tags?**  
A: They're preserved! Your new geometry + old tags = perfect

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+Alt+M` | Merge & Fix |
| `Ctrl+Z` | Undo merge |
| `A` | Building tool |
| `Q` | Square building |

## Troubleshooting

**Plugin won't load:**
- Check Java version (needs Java 8+)
- Check JOSM version (needs 18729+)
- Look at JOSM console for errors

**Merge doesn't work:**
- Ensure buildings have `building=*` tag
- Check there's overlap > 50%
- Verify you have both new and old buildings

**Upload fails:**
- Review changeset comment
- Check for validation errors
- Ensure you're logged in to OSM

## Next Steps

📖 Read the full [User Guide](USER_GUIDE.md)  
🔧 Check [Technical Documentation](TECHNICAL.md)  
🧪 Run the [Testing Suite](TESTING.md)  
🐛 Report issues on [GitHub](https://github.com/SpatialCollectiveLtd/swaplugin/issues)

## Video Tutorial

[Coming Soon] - A step-by-step video walkthrough

## Need Help?

- **GitHub Issues:** https://github.com/SpatialCollectiveLtd/swaplugin/issues
- **JOSM Help:** https://josm.openstreetmap.de/wiki/Help
- **OSM Community:** https://community.openstreetmap.org/

---

**Happy Mapping! 🗺️**
