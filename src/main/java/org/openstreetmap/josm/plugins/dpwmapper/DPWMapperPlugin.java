package org.openstreetmap.josm.plugins.dpwmapper;

import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * DPW Mapper Support Plugin - The Clean Slate Tool
 * 
 * This plugin automates the workflow for youth mappers:
 * 1. Auto-hides existing OSM data when downloaded (clean slate view)
 * 2. Allows mappers to trace buildings on blank imagery
 * 3. Automatically merges new geometries onto old IDs to preserve history
 */
public class DPWMapperPlugin extends Plugin {
    
    private AutoHideListener autoHideListener;
    
    public DPWMapperPlugin(PluginInformation info) {
        super(info);
        
        // Initialize auto-hide listener
        autoHideListener = new AutoHideListener();
        
        // Register the Merge & Fix action
        MainMenu.add(MainApplication.getMenu().toolsMenu, new MergeAndFixAction());
    }
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Add listener to detect data downloads
            MainApplication.getLayerManager().addLayerChangeListener(autoHideListener);
        } else if (oldFrame != null) {
            // Cleanup when map frame is destroyed
            MainApplication.getLayerManager().removeLayerChangeListener(autoHideListener);
        }
    }
}
