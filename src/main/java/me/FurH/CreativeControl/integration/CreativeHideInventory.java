package me.FurH.CreativeControl.integration;

import org.bukkit.event.player.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.internals.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.configuration.*;
import org.bukkit.event.*;

public class CreativeHideInventory implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.data_hide) {
            try {
                InternalManager.getEntityPlayer(e.getPlayer(), true).hideInventory();
            }
            catch (CoreException ex) {
                CreativeControl.getPlugin().error(ex, "Failed to hide '" + e.getPlayer().getName() + "' inventory!", new Object[0]);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAuth(final CreativeUniversalLogin e) {
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.data_hide) {
            try {
                InternalManager.getEntityPlayer(e.getPlayer(), true).unHideInventory();
            }
            catch (CoreException ex) {
                CreativeControl.getPlugin().error(ex, "Failed to restore '" + e.getPlayer().getName() + "' inventory!", new Object[0]);
            }
        }
    }
}
