package me.FurH.CreativeControl.listener;

import org.bukkit.event.player.*;
import me.FurH.CreativeControl.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.player.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.region.*;
import org.bukkit.*;
import org.bukkit.event.*;

public class CreativeMoveListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void PlayerMoveEvent(final PlayerMoveEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY() && e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
            return;
        }
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final Location loc = e.getTo();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        if (config.world_exclude) {
            return;
        }
        CreativeRegion region = CreativeControl.getRegioner().getRegion(loc);
        if (region != null) {
            final World w = region.start.getWorld();
            if (w != world) {
                return;
            }
            final GameMode type = region.gamemode;
            final String typeName = type.toString().toLowerCase();
            if (!p.getGameMode().equals((Object)type) && !plugin.hasPerm((CommandSender)p, "Region.Keep")) {
                com.msg((CommandSender)p, messages.region_welcome, typeName);
                p.setGameMode(type);
            }
        }
        else if (!p.getGameMode().equals((Object)config.world_gamemode)) {
            if (plugin.hasPerm((CommandSender)p, "World.Keep")) {
                return;
            }
            if (plugin.hasPerm((CommandSender)p, "World.Keep." + e.getTo().getWorld().getName())) {
                return;
            }
            region = CreativeControl.getRegioner().getRegion(e.getFrom());
            PlayerUtils.toSafeLocation(p);
            if (region != null) {
                com.msg((CommandSender)p, messages.region_farewell, region.gamemode.toString().toLowerCase());
            }
            p.setGameMode(config.world_gamemode);
        }
    }
}
