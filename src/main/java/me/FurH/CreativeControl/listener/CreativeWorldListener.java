package me.FurH.CreativeControl.listener;

import org.bukkit.event.*;
import me.FurH.CreativeControl.*;
import java.sql.*;
import me.FurH.CreativeControl.core.database.*;
import org.bukkit.event.world.*;
import org.bukkit.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.configuration.*;
import org.bukkit.entity.*;

public class CreativeWorldListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onWorldInit(final WorldInitEvent e) {
        this.loadWorld(e.getWorld());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onWorldLoad(final WorldLoadEvent e) {
        this.loadWorld(e.getWorld());
    }
    
    public void loadWorld(final World world) {
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (!main.config_single) {
            CreativeControl.getWorldConfig().load(world);
        }
        CreativeControl.getDb().load(null, world.getName(), null);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onStructureGrown(final StructureGrowEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getWorld());
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (config.world_exclude) {
            return;
        }
        final Player p = e.getPlayer();
        if (p == null) {
            return;
        }
        if (!e.isFromBonemeal()) {
            return;
        }
        if (!p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            return;
        }
        if (config.prevent_bonemeal && !plugin.hasPerm((CommandSender)p, "Preventions.Bonemeal")) {
            com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
            e.setCancelled(true);
        }
    }
}
