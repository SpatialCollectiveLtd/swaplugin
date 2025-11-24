package org.openstreetmap.josm.plugins.dpwmapper;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.BBox;
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
        System.out.println("\n========================================");
        System.out.println("[DPWMapper] MERGE & FIX STARTED");
        System.out.println("========================================");
        
        // Get active data layer
        OsmDataLayer layer = MainApplication.getLayerManager().getEditLayer();
        if (layer == null) {
            System.out.println("[DPWMapper] ERROR: No active data layer");
            new Notification("No active data layer found")
                .setIcon(JOptionPane.WARNING_MESSAGE)
                .show();
            return;
        }
        
        DataSet dataSet = layer.getDataSet();
        System.out.println("[DPWMapper] Active layer: " + layer.getName());
        
        // Temporarily disable the clean slate filter to access hidden objects
        System.out.println("[DPWMapper] Disabling clean slate filter...");
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
        
        System.out.println("\n[DPWMapper] === MERGE ANALYSIS ===");
        
        // Separate ways into new and old
        List<Way> newBuildings = dataSet.getWays().stream()
            .filter(w -> w.isNew() && !w.isDeleted() && w.isClosed() && w.hasTag("building"))
            .collect(Collectors.toList());
            
        List<Way> oldBuildings = dataSet.getWays().stream()
            .filter(w -> !w.isNew() && !w.isDeleted() && w.isClosed() && w.hasTag("building"))
            .collect(Collectors.toList());
        
        result.newBuildingCount = newBuildings.size();
        
        System.out.println("[DPWMapper] New buildings: " + newBuildings.size());
        System.out.println("[DPWMapper] Old buildings: " + oldBuildings.size());
        
        // Track which old buildings have been matched
        Set<Way> matchedOldBuildings = new HashSet<>();
        
        // Process each new building
        for (int i = 0; i < newBuildings.size(); i++) {
            Way newBuilding = newBuildings.get(i);
            System.out.println("\n[DPWMapper] Processing new building " + (i+1) + "/" + newBuildings.size());
            System.out.println("[DPWMapper] New building ID: " + newBuilding.getId());
            
            Way bestMatch = findBestMatch(newBuilding, oldBuildings, matchedOldBuildings);
            
            if (bestMatch != null) {
                System.out.println("[DPWMapper] MATCH FOUND with old building ID: " + bestMatch.getId());
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
                    System.out.println("[DPWMapper] Successfully merged");
                    
                } catch (Exception ex) {
                    System.out.println("[DPWMapper] MERGE FAILED: " + ex.getMessage());
                    ex.printStackTrace();
                    result.conflicts.add(newBuilding);
                    result.conflictCount++;
                }
            } else {
                System.out.println("[DPWMapper] No match found - keeping as new building");
            }
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
        
        int candidateCount = 0;
        
        for (Way oldBuilding : oldBuildings) {
            if (alreadyMatched.contains(oldBuilding)) {
                continue;
            }
            
            // Fast check: Bounding box intersection
            if (!newBuilding.getBBox().intersects(oldBuilding.getBBox())) {
                continue;
            }
            
            candidateCount++;
            System.out.println("  [DPWMapper] Checking overlap with old building ID: " + oldBuilding.getId());
            
            // Precise check: Calculate overlap percentage
            double overlap = calculateOverlapPercentage(newBuilding, oldBuilding);
            
            System.out.println("  [DPWMapper] Overlap: " + String.format("%.2f%%", overlap * 100) + " (threshold: " + (OVERLAP_THRESHOLD * 100) + "%)");
            
            if (overlap > maxOverlap) {
                maxOverlap = overlap;
                bestMatch = oldBuilding;
                System.out.println("  [DPWMapper] NEW BEST MATCH (" + String.format("%.2f%%", overlap * 100) + ")");
            }
        }
        
        System.out.println("  [DPWMapper] Checked " + candidateCount + " candidates, best overlap: " + String.format("%.2f%%", maxOverlap * 100));
        
        return bestMatch;
    }
    
    /**
     * Calculates what percentage of the old building is covered by the new building.
     * Uses a combination of bounding box overlap and node proximity.
     */
    private double calculateOverlapPercentage(Way newWay, Way oldWay) {
        try {
            // Calculate areas
            double newArea = Geometry.computeArea(newWay);
            double oldArea = Geometry.computeArea(oldWay);
            
            if (oldArea == 0 || newArea == 0) {
                System.out.println("    [DPWMapper] Zero area detected");
                return 0;
            }
            
            System.out.println("    [DPWMapper] New area: " + String.format("%.2f", newArea) + " sq m");
            System.out.println("    [DPWMapper] Old area: " + String.format("%.2f", oldArea) + " sq m");
            
            // Get bounding boxes
            BBox newBBox = newWay.getBBox();
            BBox oldBBox = oldWay.getBBox();
            
            // Calculate bounding box intersection area
            double bboxOverlap = calculateBBoxOverlap(newBBox, oldBBox, oldBBox);
            System.out.println("    [DPWMapper] BBox overlap: " + String.format("%.0f%%", bboxOverlap * 100));
            
            // If bounding boxes barely overlap, no need to check further
            if (bboxOverlap < 0.3) {
                System.out.println("    [DPWMapper] BBox overlap too low, skipping");
                return 0;
            }
            
            // Calculate node-based overlap (bidirectional)
            double nodeOverlap = calculateNodeOverlap(newWay, oldWay);
            System.out.println("    [DPWMapper] Node overlap: " + String.format("%.0f%%", nodeOverlap * 100));
            
            // Combine both metrics: use average weighted towards node overlap
            double combinedOverlap = (bboxOverlap * 0.3) + (nodeOverlap * 0.7);
            System.out.println("    [DPWMapper] Combined overlap: " + String.format("%.0f%%", combinedOverlap * 100));
            
            return combinedOverlap;
            
        } catch (Exception e) {
            System.out.println("    [DPWMapper] calculateOverlapPercentage ERROR: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Calculate bounding box overlap percentage.
     */
    private double calculateBBoxOverlap(BBox bbox1, BBox bbox2, BBox referenceBBox) {
        // Calculate intersection rectangle
        double intersectMinLat = Math.max(bbox1.getBottomRight().lat(), bbox2.getBottomRight().lat());
        double intersectMaxLat = Math.min(bbox1.getTopLeft().lat(), bbox2.getTopLeft().lat());
        double intersectMinLon = Math.max(bbox1.getTopLeft().lon(), bbox2.getTopLeft().lon());
        double intersectMaxLon = Math.min(bbox1.getBottomRight().lon(), bbox2.getBottomRight().lon());
        
        if (intersectMinLat >= intersectMaxLat || intersectMinLon >= intersectMaxLon) {
            return 0; // No intersection
        }
        
        // Calculate areas (approximation using lat/lon degrees)
        double intersectArea = (intersectMaxLat - intersectMinLat) * (intersectMaxLon - intersectMinLon);
        double referenceArea = (referenceBBox.getTopLeft().lat() - referenceBBox.getBottomRight().lat()) * 
                               (referenceBBox.getBottomRight().lon() - referenceBBox.getTopLeft().lon());
        
        if (referenceArea == 0) {
            return 0;
        }
        
        return intersectArea / referenceArea;
    }
    
    /**
     * Calculate node-based overlap using bidirectional check.
     */
    private double calculateNodeOverlap(Way newWay, Way oldWay) {
        List<Node> newNodes = newWay.getNodes();
        List<Node> oldNodes = oldWay.getNodes();
        
        if (newNodes.isEmpty() || oldNodes.isEmpty()) {
            return 0;
        }
        
        // BIDIRECTIONAL CHECK: Count nodes from BOTH ways that are inside the OTHER
        int newNodesInsideOld = 0;
        for (Node newNode : newNodes) {
            if (Geometry.nodeInsidePolygon(newNode, oldNodes)) {
                newNodesInsideOld++;
            }
        }
        
        int oldNodesInsideNew = 0;
        for (Node oldNode : oldNodes) {
            if (Geometry.nodeInsidePolygon(oldNode, newNodes)) {
                oldNodesInsideNew++;
            }
        }
        
        double newInsidePercentage = (double) newNodesInsideOld / newNodes.size();
        double oldInsidePercentage = (double) oldNodesInsideNew / oldNodes.size();
        
        System.out.println("    [DPWMapper] New nodes inside old: " + newNodesInsideOld + "/" + newNodes.size() + " (" + String.format("%.0f%%", newInsidePercentage * 100) + ")");
        System.out.println("    [DPWMapper] Old nodes inside new: " + oldNodesInsideNew + "/" + oldNodes.size() + " (" + String.format("%.0f%%", oldInsidePercentage * 100) + ")");
        
        // Use the MAXIMUM of both percentages
        return Math.max(newInsidePercentage, oldInsidePercentage);
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
