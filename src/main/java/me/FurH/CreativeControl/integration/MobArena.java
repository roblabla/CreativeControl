package me.FurH.CreativeControl.integration;

import com.garbagemule.MobArena.events.*;
import me.FurH.CreativeControl.*;
import org.bukkit.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.core.util.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class MobArena implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onArenaJoinEvent(final ArenaPlayerJoinEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final Player p = e.getPlayer();
        if (!p.getGameMode().equals((Object)GameMode.SURVIVAL) && !plugin.hasPerm((CommandSender)p, "Integration.MobArena")) {
            com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
            e.setCancelled(true);
        }
    }
}
