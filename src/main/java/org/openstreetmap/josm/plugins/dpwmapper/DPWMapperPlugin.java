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
        System.out.println("========================================");
        System.out.println("[DPWMapper] Plugin initialized v" + info.version);
        System.out.println("[DPWMapper] Plugin loading at: " + System.currentTimeMillis());
        System.out.println("========================================");
        Logging.info("DPW Mapper Support Plugin v1.0.0 loaded");
    }
    
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            System.out.println("========================================");
            System.out.println("[DPWMapper] Map frame initialized");
            System.out.println("[DPWMapper] Registering AutoHideListener...");
            
            // Initialize and register the auto-hide listener
            if (autoHideListener == null) {
                autoHideListener = new AutoHideListener();
                System.out.println("[DPWMapper] AutoHideListener created");
            }
            MainApplication.getLayerManager().addLayerChangeListener(autoHideListener);
            System.out.println("[DPWMapper] Listener registered with LayerManager");
            
            // Register the Merge & Fix action in the Tools menu
            MainApplication.getMenu().toolsMenu.add(new MergeAndFixAction());
            System.out.println("[DPWMapper] Merge & Fix action added to Tools menu");
            System.out.println("[DPWMapper] Plugin ready!");
            System.out.println("========================================");
            
            Logging.info("DPW Mapper: Auto-hide listener and Merge & Fix action registered");
        } else if (oldFrame != null) {
            System.out.println("[DPWMapper] Map frame destroyed, cleaning up...");
            // Cleanup when map frame is destroyed
            if (autoHideListener != null) {
                MainApplication.getLayerManager().removeLayerChangeListener(autoHideListener);
                System.out.println("[DPWMapper] AutoHideListener removed");
            }
        }
    }
}
