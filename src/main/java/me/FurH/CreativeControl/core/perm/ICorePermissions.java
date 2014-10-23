package me.FurH.CreativeControl.core.perm;

import org.bukkit.command.*;
import org.bukkit.entity.*;

public interface ICorePermissions
{
    boolean has(CommandSender p0, String p1);
    
    String getPlayerPrefix(Player p0);
    
    String getPlayerSuffix(Player p0);
    
    String getGroupPrefix(Player p0);
    
    String getGroupSuffix(Player p0);
}
