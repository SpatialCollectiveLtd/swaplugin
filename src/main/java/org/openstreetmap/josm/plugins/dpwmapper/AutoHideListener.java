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
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.util.GuiHelper;

import javax.swing.JOptionPane;
import java.util.Collections;

/**
 * Listener that automatically applies a filter to hide existing OSM data
 * when a new data layer is added (typically from Tasking Manager download).
 * 
 * This creates the "Clean Slate" view for mappers.
 */
public class AutoHideListener implements LayerChangeListener, DataSetListener {
    
    private static final String FILTER_TEXT = "id:1-";
    private static final String NOTIFICATION_MESSAGE = "Clean Slate Active - Existing data hidden";
    
    @Override
    public void layerAdded(LayerAddEvent e) {
        if (e.getAddedLayer() instanceof OsmDataLayer) {
            OsmDataLayer dataLayer = (OsmDataLayer) e.getAddedLayer();
            
            // Add dataset listener to detect when data is actually downloaded
            dataLayer.getDataSet().addDataSetListener(this);
            
            // Apply filter if data already exists
            applyCleanSlateFilter(dataLayer);
        }
    }
    
    @Override
    public void dataChanged(DataChangedEvent event) {
        // When data changes (i.e., download completes), apply the filter
        DataSet dataSet = event.getDataset();
        if (dataSet != null && !dataSet.allPrimitives().isEmpty()) {
            OsmDataLayer layer = MainApplication.getLayerManager().getEditLayer();
            if (layer != null) {
                applyCleanSlateFilter(layer);
            }
        }
    }
    
    /**
     * Applies the "Clean Slate" filter to hide all existing OSM data (id > 0)
     * while keeping newly created objects (id < 0) visible.
     */
    private void applyCleanSlateFilter(OsmDataLayer layer) {
        GuiHelper.runInEDT(() -> {
            try {
                System.out.println("DPW Mapper: Attempting to apply clean slate filter...");
                
                // Check if map and filter dialog exist
                if (MainApplication.getMap() == null) {
                    System.err.println("DPW Mapper: Map is null, cannot apply filter");
                    return;
                }
                
                if (MainApplication.getMap().filterDialog == null) {
                    System.err.println("DPW Mapper: Filter dialog is null, cannot apply filter");
                    return;
                }
                
                // Get or create the filter table model
                org.openstreetmap.josm.gui.dialogs.FilterTableModel filterModel = MainApplication.getMap().filterDialog.getFilterModel();
                
                if (filterModel == null) {
                    System.err.println("DPW Mapper: Filter model is null");
                    return;
                }
                
                System.out.println("DPW Mapper: Filter model found with " + filterModel.getRowCount() + " filters");
                
                // Check if our filter already exists
                boolean filterExists = false;
                for (int i = 0; i < filterModel.getRowCount(); i++) {
                    org.openstreetmap.josm.data.osm.Filter filter = filterModel.getValue(i);
                    if (filter != null && FILTER_TEXT.equals(filter.text)) {
                        // Filter exists, just enable it
                        System.out.println("DPW Mapper: Filter already exists, enabling it");
                        filter.enable = true;
                        filter.hiding = true;
                        filterExists = true;
                        break;
                    }
                }
                
                // If filter doesn't exist, create it
                if (!filterExists) {
                    System.out.println("DPW Mapper: Creating new filter: " + FILTER_TEXT);
                    org.openstreetmap.josm.data.osm.Filter newFilter = new org.openstreetmap.josm.data.osm.Filter();
                    newFilter.text = FILTER_TEXT;
                    newFilter.hiding = true;  // Hide matching objects
                    newFilter.enable = true;   // Enable the filter
                    newFilter.inverted = false;
                    
                    filterModel.addFilter(newFilter);
                    System.out.println("DPW Mapper: Filter added to model");
                }
                
                // Execute the filter
                filterModel.executeFilters();
                System.out.println("DPW Mapper: Filters executed successfully");
                
                // Notify the user
                new Notification(NOTIFICATION_MESSAGE)
                    .setIcon(JOptionPane.INFORMATION_MESSAGE)
                    .setDuration(Notification.TIME_SHORT)
                    .show();
                    
            } catch (Exception ex) {
                ex.printStackTrace();
                new Notification("Failed to apply Clean Slate filter: " + ex.getMessage())
                    .setIcon(JOptionPane.ERROR_MESSAGE)
                    .show();
            }
        });
    }
    
    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() instanceof OsmDataLayer) {
            OsmDataLayer dataLayer = (OsmDataLayer) e.getRemovedLayer();
            dataLayer.getDataSet().removeDataSetListener(this);
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
