# Testing Guide

## Testing the DPW Mapper Plugin

### Prerequisites
- JOSM installed (latest stable version)
- Sample OSM data with buildings
- Access to HOT Tasking Manager (or local OSM data)

### Test Suite

## 1. Installation Tests

### Test 1.1: Plugin Installation
**Steps:**
1. Build plugin: `./gradlew jar`
2. Copy JAR to JOSM plugins directory
3. Start JOSM
4. Go to Edit → Preferences → Plugins

**Expected:**
- Plugin appears in list as "DPW Mapper Support Plugin"
- Can be enabled/disabled
- No error messages in console

### Test 1.2: Plugin Loading
**Steps:**
1. Enable plugin
2. Restart JOSM
3. Check Tools menu

**Expected:**
- "Merge & Fix" appears in Tools menu
- No startup errors in console

## 2. Auto-Hide Filter Tests

### Test 2.1: Filter Activation on Download
**Steps:**
1. Download OSM data (File → Download Data)
2. Select area with existing buildings
3. Click Download

**Expected:**
- Notification appears: "Clean Slate Active - Existing data hidden"
- Windows → Filter shows "id:1-" filter enabled
- Map appears empty (only imagery visible)
- Existing buildings are hidden

### Test 2.2: New Objects Remain Visible
**Steps:**
1. With filter active, draw a new building
2. Tag it with `building=yes`

**Expected:**
- New building is visible
- Can select and edit new building
- Filter doesn't hide new objects (id < 0)

### Test 2.3: Filter Persistence
**Steps:**
1. Download data (filter activates)
2. Close JOSM
3. Reopen JOSM with same data

**Expected:**
- Filter remains in filter list
- May need to be manually re-enabled

### Test 2.4: Manual Filter Toggle
**Steps:**
1. Open Windows → Filter
2. Disable "id:1-" filter

**Expected:**
- Existing buildings become visible
- Can re-enable filter to hide again

## 3. Merge & Fix Tests

### Test 3.1: Perfect Overlap Scenario
**Steps:**
1. Download area with 1 existing building
2. Filter activates (building hidden)
3. Draw new building in exact same location (> 50% overlap)
4. Tag with `building=yes`
5. Click Tools → Merge & Fix

**Expected:**
- Notification: "Merged 1 buildings. 0 conflicts found."
- Old building ID retained
- Old building has new geometry
- New building deleted
- Old building tags preserved
- Filter disabled (can see result)

### Test 3.2: No Overlap Scenario
**Steps:**
1. Download area with existing buildings
2. Draw new building in empty area (no overlap)
3. Click Merge & Fix

**Expected:**
- Notification: "Merged 0 buildings. 0 conflicts found."
- New building remains as-is
- Will be uploaded as brand new building

### Test 3.3: Partial Overlap (< 50%)
**Steps:**
1. Download area with existing building
2. Draw new building with < 50% overlap
3. Click Merge & Fix

**Expected:**
- No merge occurs
- Both buildings remain separate
- Treated as new building

### Test 3.4: High Overlap (> 50%)
**Steps:**
1. Download area with existing building
2. Draw new building with > 50% overlap
3. Click Merge & Fix

**Expected:**
- Merge occurs
- Old building gets new geometry
- New building deleted

### Test 3.5: Multiple Non-Conflicting Merges
**Steps:**
1. Download area with 5 existing buildings
2. Draw 5 new buildings, each overlapping one old building
3. Click Merge & Fix

**Expected:**
- Notification: "Merged 5 buildings. 0 conflicts found."
- All 5 old buildings updated with new geometry
- All 5 new buildings deleted

### Test 3.6: Conflict Detection (Multiple New → One Old)
**Steps:**
1. Download area with 1 existing large building
2. Draw 2 new buildings, both overlapping the same old building
3. Click Merge & Fix

**Expected:**
- Notification: "Merged 0 buildings. 2 conflicts found."
- Both new buildings selected
- User must resolve manually

### Test 3.7: Tag Preservation
**Steps:**
1. Download building with tags: `building=house`, `addr:street=Main St`
2. Draw new building over it (only tag: `building=yes`)
3. Click Merge & Fix

**Expected:**
- Old building retains: `building=house`, `addr:street=Main St`
- Old building gets new geometry
- New tags don't overwrite old tags

### Test 3.8: Undo Functionality
**Steps:**
1. Perform a merge (multiple buildings)
2. Press Ctrl+Z

**Expected:**
- Entire merge operation undone
- Old buildings back to original geometry
- New buildings restored
- Filter state restored

### Test 3.9: Redo Functionality
**Steps:**
1. Perform merge
2. Undo (Ctrl+Z)
3. Redo (Ctrl+Y)

**Expected:**
- Merge re-applied
- Same result as original merge

## 4. Edge Case Tests

### Test 4.1: No Buildings Tagged
**Steps:**
1. Download data
2. Draw shapes without `building=*` tag
3. Click Merge & Fix

**Expected:**
- No merges occur
- Only objects with `building` tag are processed

### Test 4.2: Open Ways (Not Closed)
**Steps:**
1. Draw open way (road-like)
2. Tag with `building=yes` (incorrectly)
3. Click Merge & Fix

**Expected:**
- Open ways ignored
- Only closed ways processed

### Test 4.3: Empty Data Layer
**Steps:**
1. Create new layer with no data
2. Click Merge & Fix

**Expected:**
- Notification: "No active data layer found" or "No buildings to merge"

### Test 4.4: No New Buildings
**Steps:**
1. Download data (only old buildings visible after disabling filter)
2. Don't draw anything new
3. Click Merge & Fix

**Expected:**
- Notification: "No buildings to merge"

### Test 4.5: Very Large Dataset
**Steps:**
1. Download large area (1000+ buildings)
2. Draw 100 new buildings
3. Click Merge & Fix

**Expected:**
- Operation completes (may take a few seconds)
- Correct number of merges reported
- JOSM remains responsive

## 5. Integration Tests

### Test 5.1: Tasking Manager Workflow
**Steps:**
1. Go to HOT Tasking Manager
2. Select task
3. Click "Edit with JOSM"
4. JOSM opens with data downloaded
5. Follow complete workflow

**Expected:**
- Filter auto-activates
- Can map on clean slate
- Merge works correctly
- Can upload successfully

### Test 5.2: Multiple Mapping Sessions
**Steps:**
1. Download data, map some buildings, merge
2. Upload
3. Download same area again
4. Map more buildings, merge
5. Upload

**Expected:**
- Each session works independently
- No data corruption
- History preserved correctly

### Test 5.3: Upload After Merge
**Steps:**
1. Perform merge
2. Click Upload
3. Check changeset

**Expected:**
- Only modified old buildings uploaded
- Deleted new buildings not in changeset
- Tags preserved
- Geometry updated

## 6. Performance Tests

### Test 6.1: Merge Speed
**Scenario:** 100 new buildings, 100 old buildings

**Expected:**
- Merge completes in < 5 seconds

### Test 6.2: Filter Activation Speed
**Scenario:** Download area with 1000 buildings

**Expected:**
- Filter applies in < 2 seconds

## 7. UI Tests

### Test 7.1: Keyboard Shortcut
**Steps:**
1. Download data with buildings
2. Press Ctrl+Alt+M

**Expected:**
- Merge & Fix action triggered
- Same as clicking menu item

### Test 7.2: Notification Display
**Steps:**
1. Perform various actions

**Expected:**
- Notifications appear in correct position
- Auto-dismiss after appropriate time
- Don't block workflow

### Test 7.3: Conflict Selection
**Steps:**
1. Create conflict scenario
2. Click Merge & Fix

**Expected:**
- Conflicted objects highlighted
- Can see selection in layer panel
- Can work with selected objects

## Test Results Template

```
Test ID: _________
Date: _________
Tester: _________
JOSM Version: _________
Plugin Version: _________

Status: [ ] PASS [ ] FAIL [ ] SKIP

Notes:
_________________________________________
_________________________________________

Issues Found:
_________________________________________
_________________________________________
```

## Automated Testing (Future)

### Unit Tests
Create JUnit tests for:
- Overlap calculation
- Conflict detection
- Command generation

### Integration Tests
Use JOSM test framework:
- Mock data downloads
- Simulate user actions
- Verify command sequences

## Bug Reporting

If you find a bug during testing:

1. **Reproduce:** Can you make it happen again?
2. **Document:** What steps lead to the bug?
3. **Evidence:** Screenshot or error message
4. **Report:** Create GitHub issue with:
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - JOSM version
   - Plugin version
   - Error logs (if any)

## Test Data

### Sample OSM Data
Download test areas from:
- overpass-turbo.eu
- JOSM File → Download Data
- HOT Tasking Manager (test projects)

### Recommended Test Areas
- Urban area with mix of building sizes
- Rural area with sparse buildings
- Area with complex building shapes
- Area with well-tagged buildings
