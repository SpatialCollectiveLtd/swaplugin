package org.openstreetmap.josm.plugins.dpwmapper;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Logging;

/**
 * DPW Mapper Support Plugin - The Clean Slate Tool
 * 
 * This plugin automates the workflow for youth mappers:
 * 1. Auto-hides existing OSM data when downloaded (clean slate view)
 * 2. Allows mappers to trace buildings on blank imagery
 * 3. Automatically merges new geometries onto old IDs to preserve history
 * 
 * @author Spatial Collective Ltd
 * @version 1.0.0
 */
public class DPWMapperPlugin extends Plugin {
    
    private AutoHideListener autoHideListener;
    
    public DPWMapperPlugin(PluginInformation info) {
        super(info);
        Logging.info("DPW Mapper Support Plugin v1.0.0 loaded");
    }
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Initialize and register the auto-hide listener
            if (autoHideListener == null) {
                autoHideListener = new AutoHideListener();
            }
            MainApplication.getLayerManager().addLayerChangeListener(autoHideListener);
            
            // Register the Merge & Fix action in the Tools menu
            MainApplication.getMenu().toolsMenu.add(new MergeAndFixAction());
            
            Logging.info("DPW Mapper: Auto-hide listener and Merge & Fix action registered");
        } else if (oldFrame != null) {
            // Cleanup when map frame is destroyed
            if (autoHideListener != null) {
                MainApplication.getLayerManager().removeLayerChangeListener(autoHideListener);
            }
        }
    }
}
