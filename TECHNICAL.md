# DPW Mapper Plugin - Technical Documentation

## Architecture Overview

The plugin consists of three main components:

### 1. DPWMapperPlugin (Main Class)
- Entry point for the plugin
- Registers the AutoHideListener
- Adds the MergeAndFixAction to the Tools menu
- Manages plugin lifecycle

### 2. AutoHideListener
- Listens for layer addition events
- Listens for dataset changes (data downloads)
- Automatically applies the "id:1-" filter when OSM data is loaded
- Creates the "Clean Slate" view

### 3. MergeAndFixAction
- Provides the "Merge & Fix" toolbar action
- Implements the spatial matching algorithm
- Creates and executes geometry transfer commands
- Handles conflict detection

## Algorithm Details

### Auto-Hide Filter

**Trigger:** 
- `LayerAddEvent` when `OsmDataLayer` is added
- `DataChangedEvent` when data is downloaded

**Implementation:**
```java
Filter filter = new Filter();
filter.text = "id:1-";
filter.hiding = true;  // Hide matching objects
filter.enable = true;  // Enable the filter
```

**Effect:**
- Objects with `id >= 1` (existing OSM data) are hidden
- Objects with `id < 0` (newly created) remain visible

### Merge Algorithm

**Step 1: Separation**
```java
List<Way> newBuildings = ways.filter(w -> w.isNew() && w.hasTag("building"))
List<Way> oldBuildings = ways.filter(w -> !w.isNew() && w.hasTag("building"))
```

**Step 2: Spatial Matching**
```java
for each newBuilding:
    for each oldBuilding:
        if BoundingBoxes.intersect(newBuilding, oldBuilding):
            overlap = calculateOverlapPercentage(newBuilding, oldBuilding)
            if overlap > 0.50:
                match = oldBuilding
                break
```

**Step 3: Overlap Calculation**
```java
double calculateOverlapPercentage(Way new, Way old):
    oldArea = Geometry.computeArea(old)
    intersectionArea = Geometry.getAreaOfIntersection(new, old)
    return intersectionArea / oldArea
```

**Step 4: Geometry Transfer**
```java
if match found:
    Way updated = new Way(oldBuilding)
    updated.setNodes(newBuilding.getNodes())
    // Tags remain from oldBuilding
    
    commands.add(new ChangeCommand(oldBuilding, updated))
    commands.add(new DeleteCommand(newBuilding))
```

**Step 5: Transaction**
```java
SequenceCommand transaction = new SequenceCommand("DPW Auto-Merge", commands)
Main.undoRedo.add(transaction)
```

## Data Flow

```
User clicks "Start Editor" in Tasking Manager
    ↓
JOSM downloads OSM data
    ↓
LayerAddEvent fired
    ↓
AutoHideListener.layerAdded()
    ↓
Filter "id:1-" applied
    ↓
User sees clean slate (only imagery)
    ↓
User traces buildings (creates new Ways with id < 0)
    ↓
User clicks "Merge & Fix"
    ↓
MergeAndFixAction.actionPerformed()
    ↓
Filter temporarily disabled
    ↓
Spatial matching performed
    ↓
Commands created (ChangeCommand + DeleteCommand)
    ↓
SequenceCommand executed
    ↓
Filter remains disabled for review
    ↓
User uploads to OSM
```

## Command Pattern

The plugin uses JOSM's command pattern for all modifications:

### ChangeCommand
- Modifies existing OSM objects
- Used to update old building geometry
- Preserves object ID and tags

### DeleteCommand
- Deletes OSM objects
- Used to remove temporary new buildings
- Marks object as deleted in the dataset

### SequenceCommand
- Wraps multiple commands into one transaction
- Enables single-step undo/redo
- Used for the entire merge operation

## Performance Optimizations

### Bounding Box Pre-filtering
Before expensive geometry calculations:
```java
if (!newWay.getBBox().intersects(oldWay.getBBox())) {
    continue; // Skip this pair
}
```

### Early Exit
When match found:
```java
if (overlap > threshold) {
    bestMatch = oldBuilding;
    break; // Don't check remaining old buildings
}
```

### Deduplication
Track matched buildings to avoid double-matching:
```java
Set<Way> matchedOldBuildings = new HashSet<>();
if (matchedOldBuildings.contains(oldBuilding)) {
    continue;
}
```

## Error Handling

### Conflict Scenarios

**1. Multiple New → One Old**
```
New Building A --|
                 |--> Old Building X
New Building B --|
```
Action: Mark both new buildings as conflicts, select for review

**2. One New → Multiple Old**
```
               |--> Old Building X
New Building A-|
               |--> Old Building Y
```
Action: Mark new building as conflict, select for review

**3. Geometry Calculation Fails**
```java
try {
    overlap = calculateOverlapPercentage(new, old)
} catch (Exception e) {
    overlap = 0 // Assume no overlap
}
```

### Safety Mechanisms

1. **Tag Preservation:** Always keep old tags
2. **Transaction Wrapping:** All changes in one undo step
3. **Conflict Selection:** Problematic objects auto-selected
4. **Filter Cleanup:** Filter disabled after merge for review

## API Dependencies

### JOSM Core APIs Used

**Data Model:**
- `org.openstreetmap.josm.data.osm.Way`
- `org.openstreetmap.josm.data.osm.Node`
- `org.openstreetmap.josm.data.osm.DataSet`
- `org.openstreetmap.josm.data.osm.OsmPrimitive`

**Commands:**
- `org.openstreetmap.josm.command.ChangeCommand`
- `org.openstreetmap.josm.command.DeleteCommand`
- `org.openstreetmap.josm.command.SequenceCommand`

**Geometry:**
- `org.openstreetmap.josm.tools.Geometry`
  - `computeArea(Way)`
  - `getAreaOfIntersection(Way, Way)`

**UI:**
- `org.openstreetmap.josm.gui.Notification`
- `org.openstreetmap.josm.gui.layer.OsmDataLayer`
- `org.openstreetmap.josm.data.osm.Filter`

**Events:**
- `org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener`
- `org.openstreetmap.josm.data.osm.event.DataSetListener`

## Testing Strategy

### Unit Tests (Future Enhancement)
- Test overlap calculation with known geometries
- Test conflict detection scenarios
- Test tag preservation

### Integration Tests
- Test filter application on real OSM data
- Test merge with various building configurations
- Test undo/redo functionality

### Manual Testing Checklist
- [ ] Filter auto-applies on download
- [ ] New buildings remain visible
- [ ] Merge button appears in Tools menu
- [ ] Perfect overlap (>50%) merges correctly
- [ ] No overlap (<50%) keeps buildings separate
- [ ] Conflicts are detected and selected
- [ ] Tags are preserved from old buildings
- [ ] Ctrl+Z undoes entire merge
- [ ] Upload works correctly

## Extension Points

### Custom Overlap Threshold
Modify `OVERLAP_THRESHOLD` constant:
```java
private static final double OVERLAP_THRESHOLD = 0.50; // Change to 0.60 for 60%
```

### Custom Filter Text
Modify `FILTER_TEXT` constant:
```java
private static final String FILTER_TEXT = "id:1-"; // Modify as needed
```

### Additional Geometry Types
Extend to support other OSM primitives:
```java
// Currently only supports Ways
// Could add Node, Relation support
```

### Tag Merging Strategies
Currently preserves old tags. Could implement:
- Merge new tags into old
- Prompt user for conflicts
- Smart tag reconciliation

## Known Limitations

1. **Only supports Way objects:** Nodes and Relations not handled
2. **Building tag required:** Objects must have `building=*` tag
3. **Closed ways only:** Open ways (roads, etc.) are ignored
4. **50% fixed threshold:** Not currently user-configurable
5. **1-to-1 matching:** Doesn't handle complex merge scenarios automatically

## Future Enhancements

- [ ] User-configurable overlap threshold
- [ ] Support for other feature types (landuse, etc.)
- [ ] Smarter conflict resolution UI
- [ ] Batch processing for large areas
- [ ] Statistics dashboard
- [ ] Export merge report
- [ ] Integration with JOSM validation
- [ ] Machine learning for better matching

## References

- JOSM Developer Guide: https://josm.openstreetmap.de/wiki/DevelopersGuide
- JOSM Plugin Development: https://josm.openstreetmap.de/wiki/DevelopersGuide/DevelopingPlugins
- utilsplugin2 Source: https://github.com/JOSM/utilsplugin2
- ReplaceGeometry Implementation: https://github.com/JOSM/utilsplugin2/blob/master/src/org/openstreetmap/josm/plugins/utilsplugin2/replacegeometry/ReplaceGeometryUtils.java
