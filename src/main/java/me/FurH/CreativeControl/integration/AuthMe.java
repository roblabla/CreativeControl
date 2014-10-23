package me.FurH.CreativeControl.integration;

import uk.org.whoami.authme.events.*;
import org.bukkit.*;
import org.bukkit.event.*;

public class AuthMe implements Listener
{
    //TODO : Fix authme
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryProtect(final ProtectInventoryEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLogin(final LoginEvent e) {
        final CreativeUniversalLogin event = new CreativeUniversalLogin(e.getPlayer());
        Bukkit.getPluginManager().callEvent((Event)event);
    }
}
