package me.FurH.CreativeControl.integration;

import org.mcsg.survivalgames.api.*;
import me.FurH.CreativeControl.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class SurvivalGames implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onJoinArenaEvent(final PlayerJoinArenaEvent e) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Player p = e.getPlayer();
        if (!p.getGameMode().equals((Object)GameMode.SURVIVAL) && !plugin.hasPerm((CommandSender)p, "Integration.SurvivalGames")) {
            p.setGameMode(GameMode.SURVIVAL);
        }
    }
}
