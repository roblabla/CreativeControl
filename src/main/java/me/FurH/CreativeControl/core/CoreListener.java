package me.FurH.CreativeControl.core;

import me.FurH.CreativeControl.core.internals.*;
import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.*;
import org.bukkit.entity.*;

public class CoreListener implements Listener
{
    private boolean inbound;
    private boolean outbound;
    
    public CoreListener(final boolean inbound, final boolean outbound) {
        super();
        this.inbound = inbound;
        this.outbound = outbound;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        try {
            if (this.inbound) {
                InternalManager.getEntityPlayer(e.getPlayer(), false).setInboundQueue();
            }
        }
        catch (CoreException ex) {
            ex.printStackTrace();
        }
        try {
            if (this.outbound) {
                InternalManager.getEntityPlayer(e.getPlayer(), false).setOutboundQueue();
            }
        }
        catch (CoreException ex) {
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        InternalManager.removeEntityPlayer(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerKickEvent e) {
        InternalManager.removeEntityPlayer(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.isCancelled()) {
            try {
                if (!(e.getWhoClicked() instanceof Player)) {
                    return;
                }
                final Player p = (Player)e.getWhoClicked();
                if (InternalManager.getEntityPlayer(p, true).isInventoryHidden()) {
                    e.setCancelled(true);
                }
            }
            catch (CoreException ex) {
                ex.printStackTrace();
            }
        }
    }
}
