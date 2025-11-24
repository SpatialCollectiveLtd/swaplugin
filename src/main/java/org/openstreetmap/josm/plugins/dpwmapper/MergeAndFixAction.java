package org.openstreetmap.josm.plugins.dpwmapper;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The "Merge & Fix" action that:
 * 1. Finds overlapping new and old buildings
 * 2. Transfers geometry from new buildings to old buildings
 * 3. Deletes temporary new buildings
 * 4. Preserves OSM history and tags
 */
public class MergeAndFixAction extends JosmAction {
    
    private static final double OVERLAP_THRESHOLD = 0.50; // 50%
    
    public MergeAndFixAction() {
        super(
            "Merge & Fix",
            (String) null,  // No icon
            "Merge new buildings into existing OSM data",
            Shortcut.registerShortcut(
                "tools:dpwmerge",
                "Tool: DPW Merge and Fix",
                KeyEvent.VK_M,
                Shortcut.ALT_CTRL
            ),
            true
        );
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Get active data layer
        OsmDataLayer layer = MainApplication.getLayerManager().getEditLayer();
        if (layer == null) {
            new Notification("No active data layer found")
                .setIcon(JOptionPane.WARNING_MESSAGE)
                .show();
            return;
        }
        
        DataSet dataSet = layer.getDataSet();
        
        // Temporarily disable the clean slate filter to access hidden objects
        disableCleanSlateFilter();
        
        try {
            // Perform the merge operation
            MergeResult result = performMerge(dataSet);
            
            // Show results to user
            if (result.commands.isEmpty()) {
                if (result.newBuildingCount == 0) {
                    new Notification("No new buildings found to merge.\n\nWorkflow: 1) Download data, 2) Trace new buildings, 3) Run Merge & Fix")
                        .setIcon(JOptionPane.INFORMATION_MESSAGE)
                        .setDuration(Notification.TIME_LONG)
                        .show();
                } else {
                    new Notification(String.format("Found %d new buildings but no overlapping matches.\n\nNew buildings preserved.", result.newBuildingCount))
                        .setIcon(JOptionPane.INFORMATION_MESSAGE)
                        .setDuration(Notification.TIME_LONG)
                        .show();
                }
            } else {
                // Execute all commands as a single undoable operation
                UndoRedoHandler.getInstance().add(
                    new SequenceCommand("DPW Auto-Merge", result.commands)
                );
                
                String message = String.format(
                    "✓ Merged %d buildings\n%d new buildings, %d conflicts",
                    result.mergedCount,
                    result.newBuildingCount - result.mergedCount,
                    result.conflictCount
                );
                
                new Notification(message)
                    .setIcon(JOptionPane.INFORMATION_MESSAGE)
                    .setDuration(Notification.TIME_LONG)
                    .show();
                
                // Select conflicted objects for manual review
                if (!result.conflicts.isEmpty()) {
                    dataSet.setSelected(result.conflicts);
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            new Notification("Merge failed: " + ex.getMessage())
                .setIcon(JOptionPane.ERROR_MESSAGE)
                .show();
        } finally {
            // Re-enable the clean slate filter
            enableCleanSlateFilter();
        }
    }
    
    /**
     * Performs the merge operation between new and old buildings.
     */
    private MergeResult performMerge(DataSet dataSet) {
        MergeResult result = new MergeResult();
        
        // Separate ways into new and old
        List<Way> newBuildings = dataSet.getWays().stream()
            .filter(w -> w.isNew() && !w.isDeleted() && w.isClosed() && w.hasTag("building"))
            .collect(Collectors.toList());
            
        List<Way> oldBuildings = dataSet.getWays().stream()
            .filter(w -> !w.isNew() && !w.isDeleted() && w.isClosed() && w.hasTag("building"))
            .collect(Collectors.toList());
        
        result.newBuildingCount = newBuildings.size();
        
        // Track which old buildings have been matched
        Set<Way> matchedOldBuildings = new HashSet<>();
        
        // Process each new building
        for (Way newBuilding : newBuildings) {
            Way bestMatch = findBestMatch(newBuilding, oldBuildings, matchedOldBuildings);
            
            if (bestMatch != null) {
                // Check for conflicts (multiple new buildings matching one old)
                if (matchedOldBuildings.contains(bestMatch)) {
                    result.conflicts.add(newBuilding);
                    result.conflictCount++;
                    continue;
                }
                
                // Create the merge commands
                try {
                    // Create a new way with old building's ID but new building's geometry
                    Way updatedWay = new Way(bestMatch);
                    updatedWay.setNodes(new ArrayList<>(newBuilding.getNodes()));
                    
                    // Preserve old building's tags (don't overwrite with new building's tags)
                    // The geometry changes, but tags stay the same
                    
                    result.commands.add(new ChangeCommand(bestMatch, updatedWay));
                    result.commands.add(new DeleteCommand(newBuilding));
                    
                    matchedOldBuildings.add(bestMatch);
                    result.mergedCount++;
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    result.conflicts.add(newBuilding);
                    result.conflictCount++;
                }
            }
            // If no match found, the new building stays as is (brand new building)
        }
        
        return result;
    }
    
    /**
     * Finds the best matching old building for a new building based on overlap.
     * Returns null if no suitable match found (overlap < 50%).
     */
    private Way findBestMatch(Way newBuilding, List<Way> oldBuildings, Set<Way> alreadyMatched) {
        Way bestMatch = null;
        double maxOverlap = OVERLAP_THRESHOLD;
        
        for (Way oldBuilding : oldBuildings) {
            if (alreadyMatched.contains(oldBuilding)) {
                continue;
            }
            
            // Fast check: Bounding box intersection
            if (!newBuilding.getBBox().intersects(oldBuilding.getBBox())) {
                continue;
            }
            
            // Precise check: Calculate overlap percentage
            double overlap = calculateOverlapPercentage(newBuilding, oldBuilding);
            
            if (overlap > maxOverlap) {
                maxOverlap = overlap;
                bestMatch = oldBuilding;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Calculates what percentage of the old building is covered by the new building.
     */
    private double calculateOverlapPercentage(Way newWay, Way oldWay) {
        try {
            // Calculate areas
            double oldArea = Geometry.computeArea(oldWay);
            if (oldArea == 0) {
                return 0;
            }
            
            // Calculate intersection area using node overlap
            double intersectionArea = calculateIntersectionArea(newWay, oldWay);
            
            // Return percentage of old building covered by new building
            return intersectionArea / oldArea;
            
        } catch (Exception e) {
            // If calculation fails, assume no overlap
            return 0;
        }
    }
    
    /**
     * Temporarily disables the clean slate filter so we can access hidden objects.
     */
    private void disableCleanSlateFilter() {
        try {
            org.openstreetmap.josm.gui.dialogs.FilterTableModel filterModel = MainApplication.getMap().filterDialog.getFilterModel();
            
            for (int i = 0; i < filterModel.getRowCount(); i++) {
                org.openstreetmap.josm.data.osm.Filter filter = filterModel.getValue(i);
                if (filter != null && "-new".equals(filter.text)) {
                    filter.enable = false;
                    filterModel.executeFilters();
                    break;
                }
            }
        } catch (Exception ex) {
            // Filter might not exist, that's okay
        }
    }
    
    /**
     * Re-enables the clean slate filter after merge is complete.
     */
    private void enableCleanSlateFilter() {
        try {
            org.openstreetmap.josm.gui.dialogs.FilterTableModel filterModel = MainApplication.getMap().filterDialog.getFilterModel();
            
            for (int i = 0; i < filterModel.getRowCount(); i++) {
                org.openstreetmap.josm.data.osm.Filter filter = filterModel.getValue(i);
                if (filter != null && "-new".equals(filter.text)) {
                    filter.enable = true;
                    filterModel.executeFilters();
                    break;
                }
            }
        } catch (Exception ex) {
            // Filter might not exist, that's okay
        }
    }
    
    /**
     * Calculate intersection area between two ways using bounding box and node proximity.
     * This is a simplified approximation since full polygon intersection is complex.
     */
    private double calculateIntersectionArea(Way newWay, Way oldWay) {
        try {
            // Get nodes from both ways
            List<Node> newNodes = newWay.getNodes();
            List<Node> oldNodes = oldWay.getNodes();
            
            if (newNodes.isEmpty() || oldNodes.isEmpty()) {
                return 0;
            }
            
            // Count how many nodes from newWay are inside or very close to oldWay
            int nodesInside = 0;
            for (Node newNode : newNodes) {
                if (Geometry.nodeInsidePolygon(newNode, oldNodes)) {
                    nodesInside++;
                }
            }
            
            // Approximate intersection as percentage of nodes inside * area of new way
            double newArea = Geometry.computeArea(newWay);
            double nodePercentage = (double) nodesInside / newNodes.size();
            
            return newArea * nodePercentage;
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Helper class to store merge operation results.
     */
    private static class MergeResult {
        List<org.openstreetmap.josm.command.Command> commands = new ArrayList<>();
        List<OsmPrimitive> conflicts = new ArrayList<>();
        int mergedCount = 0;
        int conflictCount = 0;
        int newBuildingCount = 0;
    }
}
