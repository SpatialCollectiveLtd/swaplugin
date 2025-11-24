# DPW Mapper Plugin - Troubleshooting Guide

## Is the Plugin Loading?

### Step 1: Check JOSM Console
1. In JOSM, open **Help → Show Status Report** 
2. Look for console output with lines like:
   ```
   DPW Mapper Support Plugin v1.0.0 initializing...
   DPW Mapper: Map frame initialized, registering listeners...
   DPW Mapper: AutoHideListener created
   DPW Mapper: AutoHideListener registered with LayerManager
   DPW Mapper: Merge & Fix action added to Tools menu
   ```

### Step 2: Check Plugin is Installed
1. Go to **Edit → Preferences → Plugins**
2. Search for "DPW" or "Mapper"
3. You should see "DPW Mapper Support" in the list
4. Make sure it's **checked/enabled**

### Step 3: Check Tools Menu
1. Look at **Tools** menu
2. You should see "Merge & Fix" near the bottom
3. If you don't see it, the plugin didn't load properly

## Testing Auto-Hide Filter

### What Should Happen:
1. **Before download**: No filters active
2. **Download OSM data**: Plugin should automatically:
   - Create filter with text: `id:1-`
   - Enable filter
   - Show notification: "Clean Slate Active - Existing data hidden"
   - Console shows:
     ```
     DPW Mapper: Attempting to apply clean slate filter...
     DPW Mapper: Filter model found with X filters
     DPW Mapper: Creating new filter: id:1-
     DPW Mapper: Filter added to model
     DPW Mapper: Filters executed successfully
     ```
3. **Result**: All existing buildings (ID > 0) should be hidden

### If Buildings Are Still Visible:

#### Check Filter Dialog
1. Open **Windows → Filter** (or press Alt+Shift+F)
2. Look for a filter with text: `id:1-`
3. Check if it's:
   - ✅ Enabled (checkbox ticked)
   - ✅ "H" column checked (hiding mode)
   - ✅ Not inverted

#### Manual Test - Create Filter Yourself
1. Open Filter dialog (Alt+Shift+F)
2. Click "Add" button
3. Enter text: `id:1-`
4. Click "Submit"
5. Enable the filter (check the boxes)
6. Click "H" column to enable hiding mode
7. **Do buildings disappear now?**
   - **YES**: Plugin logic is working, just not triggering automatically
   - **NO**: Filter syntax might be wrong or JOSM version issue

## Common Issues

### Issue 1: Plugin JAR Not in Right Location
**Symptoms**: Plugin doesn't appear in preferences
**Fix**:
- Windows: `%APPDATA%\JOSM\plugins\DPWMapper-1.0.0.jar`
- Linux: `~/.local/share/JOSM/plugins/DPWMapper-1.0.0.jar`
- Mac: `~/Library/JOSM/plugins/DPWMapper-1.0.0.jar`

### Issue 2: Wrong JOSM Version
**Symptoms**: Plugin loads but features don't work
**Fix**: Update JOSM to version 18729 or higher

### Issue 3: Filter Dialog Not Open
**Symptoms**: No error but nothing happens
**Possible cause**: JOSM might need Filter dialog to be initialized first
**Try**: Open Filter dialog manually (Alt+Shift+F) then download data again

### Issue 4: Listener Not Triggering
**Symptoms**: Console shows plugin loaded but no filter messages when downloading
**Debug**:
1. Check console for any Java exceptions
2. Try downloading data with Filter dialog open
3. Check if layer was actually added (Layers panel)

## Manual Workflow (If Auto-Hide Fails)

1. Download OSM data
2. Open Filter dialog (Alt+Shift+F)
3. Add filter: `id:1-`
4. Enable it and set to Hide mode
5. Trace your new buildings (they'll have negative IDs)
6. Press Alt+M (Merge & Fix)
7. Review results

## Getting More Debug Info

### Enable JOSM Debug Mode
1. Start JOSM with: `java -jar josm.jar --debug`
2. Or set environment variable: `JOSM_DEBUG=true`
3. This shows more console output

### Check for Exceptions
Look in console for lines starting with:
- `DPW Mapper: Error applying clean slate filter:`
- `java.lang.Exception`
- `NullPointerException`

## Report Issue

If filter still doesn't work, collect this info:
1. JOSM version (Help → About)
2. Console output when downloading data
3. Screenshot of Filter dialog
4. OS version
5. Whether manual filter creation works

---

**Updated JAR**: The latest version has enhanced logging. Replace your old JAR and restart JOSM to see debug messages.
