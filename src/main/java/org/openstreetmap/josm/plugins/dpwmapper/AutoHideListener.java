package org.openstreetmap.josm.plugins.dpwmapper;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.GuiHelper;

import javax.swing.*;
import java.awt.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listener that automatically applies a filter to hide existing OSM data
 * when data is downloaded. Shows a preparation dialog to confirm the workspace is ready.
 */
public class AutoHideListener implements LayerChangeListener, DataSetListener {
    
    private static final String FILTER_TEXT = "-new";  // Hide everything that is NOT new
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    
    @Override
    public void layerAdded(LayerAddEvent e) {
        System.out.println("[DPWMapper] ===== LAYER ADDED EVENT =====");
        System.out.println("[DPWMapper] Layer type: " + e.getAddedLayer().getClass().getName());
        
        if (e.getAddedLayer() instanceof OsmDataLayer) {
            OsmDataLayer dataLayer = (OsmDataLayer) e.getAddedLayer();
            
            System.out.println("[DPWMapper] OsmDataLayer detected: " + dataLayer.getName());
            
            // Add dataset listener to detect when data is downloaded
            dataLayer.getDataSet().addDataSetListener(this);
            System.out.println("[DPWMapper] DataSet listener registered");
            
            // IMMEDIATELY check if data already exists
            long immediateCount = dataLayer.getDataSet().getWays().stream()
                .filter(w -> !w.isDeleted() && w.hasTag("building"))
                .count();
            System.out.println("[DPWMapper] Immediate building count: " + immediateCount);
            
            if (immediateCount > 0) {
                System.out.println("[DPWMapper] Data already present, triggering preparation immediately");
                prepareWorkspace(dataLayer, (int) immediateCount);
            }
        } else {
            System.out.println("[DPWMapper] Not an OsmDataLayer, ignoring");
        }
    }
    
    @Override
    public void dataChanged(DataChangedEvent event) {
        System.out.println("[DPWMapper] ===== DATA CHANGED EVENT =====");
        System.out.println("[DPWMapper] Event source: " + event.getDataset());
        
        OsmDataLayer layer = MainApplication.getLayerManager().getEditLayer();
        if (layer == null) {
            System.out.println("[DPWMapper] No active edit layer");
            return;
        }
        
        if (isProcessing.get()) {
            System.out.println("[DPWMapper] Already processing, skipping...");
            return;
        }
        
        System.out.println("[DPWMapper] Active layer: " + layer.getName());
        
        // Count ALL ways first
        long totalWays = layer.getDataSet().getWays().size();
        System.out.println("[DPWMapper] Total ways in dataset: " + totalWays);
        
        // Count buildings
        long buildingCount = layer.getDataSet().getWays().stream()
            .filter(w -> !w.isDeleted() && w.hasTag("building"))
            .count();
        
        System.out.println("[DPWMapper] Buildings found: " + buildingCount);
        
        if (buildingCount > 0) {
            System.out.println("[DPWMapper] Triggering workspace preparation");
            prepareWorkspace(layer, (int) buildingCount);
        } else {
            System.out.println("[DPWMapper] No buildings found, not applying filter");
        }
    }
    
    /**
     * Shows a preparation dialog and applies the clean slate filter.
     */
    private void prepareWorkspace(OsmDataLayer layer, int buildingCount) {
        if (!isProcessing.compareAndSet(false, true)) {
            System.out.println("[DPWMapper] Already processing workspace preparation");
            return;
        }
        
        System.out.println("[DPWMapper] Starting workspace preparation...");
        
        GuiHelper.runInEDT(() -> {
            // Create progress dialog
            JDialog progressDialog = new JDialog((Frame) null, "DPW Mapper - Preparing Workspace", false);
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            JLabel titleLabel = new JLabel("🔄 Preparing Clean Slate Workspace");
            titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
            titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            
            JLabel statusLabel = new JLabel(String.format("Found %d buildings in downloaded data", buildingCount));
            statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            
            JLabel actionLabel = new JLabel("Hiding existing OSM data...");
            actionLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
            
            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(15));
            panel.add(statusLabel);
            panel.add(Box.createVerticalStrut(10));
            panel.add(actionLabel);
            panel.add(Box.createVerticalStrut(15));
            panel.add(progressBar);
            
            progressDialog.add(panel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(MainApplication.getMainFrame());
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            progressDialog.setVisible(true);
            
            // Apply filter in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Thread.sleep(500); // Brief delay to show the dialog
                    return applyCleanSlateFilter(layer);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    
                    try {
                        if (get()) {
                            System.out.println("[DPWMapper] Workspace preparation complete");
                            
                            new Notification("✓ Clean Slate Ready!\n\n" +
                                    String.format("Loaded %d buildings - existing data hidden.\n", buildingCount) +
                                    "Trace new buildings freely!\n" +
                                    "Use 'Merge & Fix' when done.")
                                .setIcon(JOptionPane.INFORMATION_MESSAGE)
                                .setDuration(Notification.TIME_LONG)
                                .show();
                        } else {
                            System.out.println("[DPWMapper] Filter application failed");
                            
                            new Notification("⚠ Could not activate Clean Slate filter.\n" +
                                    "You may need to manually hide existing data.")
                                .setIcon(JOptionPane.WARNING_MESSAGE)
                                .setDuration(Notification.TIME_DEFAULT)
                                .show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Notification("Error preparing workspace: " + ex.getMessage())
                            .setIcon(JOptionPane.ERROR_MESSAGE)
                            .show();
                    } finally {
                        isProcessing.set(false);
                    }
                }
            };
            
            worker.execute();
        });
    }
    
    /**
     * Apply the clean slate filter to hide existing OSM data.
     * Returns true if successful, false otherwise.
     */
    private boolean applyCleanSlateFilter(OsmDataLayer layer) {
        System.out.println("[DPWMapper] Starting applyCleanSlateFilter...");
        
        try {
            if (MainApplication.getMap() == null) {
                System.out.println("[DPWMapper] ERROR: MainApplication.getMap() is null");
                return false;
            }
            
            if (MainApplication.getMap().filterDialog == null) {
                System.out.println("[DPWMapper] ERROR: filterDialog is null");
                return false;
            }
            
            org.openstreetmap.josm.gui.dialogs.FilterTableModel filterModel = 
                MainApplication.getMap().filterDialog.getFilterModel();
            System.out.println("[DPWMapper] Got FilterTableModel: " + filterModel);
            
            // Check if filter already exists
            boolean filterExists = false;
            for (int i = 0; i < filterModel.getRowCount(); i++) {
                org.openstreetmap.josm.data.osm.Filter existingFilter = filterModel.getValue(i);
                if (existingFilter != null && FILTER_TEXT.equals(existingFilter.text)) {
                    System.out.println("[DPWMapper] Filter already exists at index " + i);
                    filterExists = true;
                    existingFilter.enable = true;
                    existingFilter.hiding = true;
                    break;
                }
            }
            
            if (!filterExists) {
                System.out.println("[DPWMapper] Creating new filter with text: " + FILTER_TEXT);
                
                // Create new filter
                org.openstreetmap.josm.data.osm.Filter filter = 
                    new org.openstreetmap.josm.data.osm.Filter();
                filter.text = FILTER_TEXT;
                filter.hiding = true;
                filter.enable = true;
                filter.inverted = false;
                
                System.out.println("[DPWMapper] Filter created: text=" + filter.text + 
                    ", hiding=" + filter.hiding + ", enable=" + filter.enable);
                
                // Add to filter model
                filterModel.addFilter(filter);
                System.out.println("[DPWMapper] Filter added to model");
            }
            
            // Execute the filters to apply them
            System.out.println("[DPWMapper] Executing filters...");
            filterModel.executeFilters();
            System.out.println("[DPWMapper] Filters executed successfully");
            
            return true;
            
        } catch (Exception ex) {
            System.out.println("[DPWMapper] ERROR in applyCleanSlateFilter: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() instanceof OsmDataLayer) {
            OsmDataLayer dataLayer = (OsmDataLayer) e.getRemovedLayer();
            dataLayer.getDataSet().removeDataSetListener(this);
            System.out.println("[DPWMapper] Layer removed: " + dataLayer.getName());
        }
    }
    
    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Not needed for this plugin
    }
    
    // Unused DataSetListener methods
    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {}
    
    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {}
    
    @Override
    public void tagsChanged(TagsChangedEvent event) {}
    
    @Override
    public void wayNodesChanged(WayNodesChangedEvent event) {}
    
    @Override
    public void relationMembersChanged(RelationMembersChangedEvent event) {}
    
    @Override
    public void nodeMoved(NodeMovedEvent event) {}
    
    @Override
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
}
