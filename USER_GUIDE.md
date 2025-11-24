# DPW Mapper Plugin - User Guide

## Quick Start

### First Time Setup
1. Install the plugin (see INSTALL.md)
2. Start JOSM
3. The plugin is ready to use automatically!

## The Clean Slate Workflow

### Step 1: Start Your Task
1. Go to HOT Tasking Manager
2. Select a task square
3. Click **"Start Editor"** (or "Edit with JOSM")
4. JOSM will open and download the OSM data

### Step 2: Clean Slate Activates Automatically
- **What happens:** The plugin immediately hides all existing buildings
- **What you see:** Only the satellite imagery (blank map)
- **Notification:** "Clean Slate Active - Existing data hidden"
- **What's actually happening:** Old data is downloaded but hidden with a filter

### Step 3: Map Buildings
1. Select the building tool (keyboard: `A`)
2. Trace all buildings you see in the imagery
3. Tag them with `building=yes` (or appropriate building type)
4. **Important:** You're drawing on a clean slate, so trace everything visible

### Step 4: Merge & Fix
1. When finished tracing, click **Tools → Merge & Fix** (or press `Ctrl+Alt+M`)
2. **What happens:**
   - Plugin finds overlaps between your new drawings and old buildings
   - Transfers your shapes to the old building IDs
   - Deletes your temporary drawings
   - Preserves all existing tags and history
3. **Result:** "Merged X buildings. Y conflicts found."

### Step 5: Review
1. The filter is automatically disabled
2. You now see the final result:
   - Old building IDs with your new accurate shapes
   - All original tags preserved (address, name, etc.)
3. If there were conflicts, they're automatically selected
   - Review these manually
   - Fix any issues

### Step 6: Upload
1. Click **Upload** (or press `Ctrl+Shift+Up`)
2. Add a changeset comment (e.g., "Remapped buildings from new imagery")
3. Upload to OpenStreetMap

## Understanding the Filter

### What is "id:1-"?
- This filter hides all objects with ID ≥ 1 (existing OSM data)
- Shows all objects with ID < 0 (your new drawings)
- You can see it in **Windows → Filter**

### Manually Toggling the Filter
If you need to see the old data before merging:
1. Open **Windows → Filter**
2. Find the "id:1-" filter
3. Uncheck "Enable" to show old data
4. Check it again to hide

## The Merge Algorithm Explained

### How It Works
1. **Spatial Matching:** For each new building you drew, the plugin:
   - Checks if it overlaps with any old building
   - Calculates the percentage of overlap
   - If overlap > 50% → Match found!

2. **Geometry Transfer:**
   - The old building ID takes on your new shape
   - Your temporary drawing is deleted
   - Tags from the old building are preserved

3. **Conflict Detection:**
   - If 2+ new buildings overlap 1 old building → Conflict
   - If 1 new building overlaps 2+ old buildings → Conflict
   - Conflicts are selected for you to review manually

### Example Scenarios

**Scenario 1: Perfect Match**
- Old building (rough shape, ID #12345)
- You draw a new building on top of it
- Merge → Old ID #12345 now has your accurate shape

**Scenario 2: No Match**
- You draw a building where none existed before
- Merge → Your new building stays as-is (brand new to OSM)

**Scenario 3: Conflict**
- One old building, but you drew it as two separate buildings
- Merge → Both selected for manual review
- You decide: Delete one? Merge manually? Keep both?

## Keyboard Shortcuts

- `Ctrl+Alt+M` - Merge & Fix
- `Ctrl+Z` - Undo the entire merge operation
- `A` - Building drawing tool

## Tips & Best Practices

### Before Mapping
- ✅ Make sure you're connected to the internet
- ✅ Check the imagery alignment
- ✅ Verify you're in the correct task square

### During Mapping
- ✅ Trace all visible buildings (it's a clean slate!)
- ✅ Square your buildings (`Q` key)
- ✅ Tag appropriately (`building=yes` minimum)
- ✅ Don't worry about "duplicates" - that's what the plugin fixes!

### After Merging
- ✅ Review any conflicts
- ✅ Check a few merged buildings look correct
- ✅ Undo (`Ctrl+Z`) if something looks wrong
- ✅ Re-merge after fixing issues

### Common Mistakes to Avoid
- ❌ Don't manually delete old buildings before merging
- ❌ Don't disable the filter and try to trace over visible data
- ❌ Don't forget to tag your buildings
- ❌ Don't skip reviewing conflicts

## Troubleshooting

### "Clean Slate didn't activate"
**Solution:** The filter might already exist
- Go to Windows → Filter
- Find "id:1-"
- Make sure it's enabled and set to "Hide"

### "Merge & Fix button is grayed out"
**Possible causes:**
- No active data layer
- No buildings to merge
**Solution:** Make sure you've downloaded data and drawn some buildings

### "Too many conflicts after merge"
**Possible causes:**
- Buildings have changed significantly since old data
- Imagery misalignment
**Solution:** 
- Check imagery offset
- Review conflicts individually
- Some may need manual fixing

### "Accidentally merged incorrectly"
**Solution:** Press `Ctrl+Z` immediately to undo the entire merge

### "Want to see old data while drawing"
**Solution:** 
- Open Windows → Filter
- Temporarily disable the "id:1-" filter
- Re-enable when ready to draw

## Advanced Usage

### Multiple Rounds of Merging
You can merge multiple times:
1. Draw some buildings → Merge
2. Draw more buildings → Merge again
3. Each merge is independent

### Partial Task Completion
1. Map what you can
2. Merge
3. Upload
4. Mark task as complete (or partially complete)

### Custom Overlap Threshold
Currently set at 50%. If you need to adjust this, contact the developer.

## Support

- **GitHub Issues:** https://github.com/SpatialCollectiveLtd/swaplugin/issues
- **JOSM Help:** https://josm.openstreetmap.de/wiki/Help

## Changelog

### Version 1.0.0 (2025-11-24)
- Initial release
- Auto-hide filter on data download
- Merge & Fix action with 50% overlap threshold
- Conflict detection and selection
- Undo support
