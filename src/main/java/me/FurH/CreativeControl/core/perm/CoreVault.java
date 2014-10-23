package me.FurH.CreativeControl.core.perm;

import net.milkbowl.vault.permission.*;
import net.milkbowl.vault.chat.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class CoreVault implements ICorePermissions
{
    private Permission permission;
    private Chat chat;
    
    public CoreVault() {
        super();
        final RegisteredServiceProvider<Permission> permissionProvider = (RegisteredServiceProvider<Permission>)Bukkit.getServicesManager().getRegistration((Class)Permission.class);
        if (permissionProvider != null) {
            this.permission = (Permission)permissionProvider.getProvider();
        }
        final RegisteredServiceProvider<Chat> chatProvider = (RegisteredServiceProvider<Chat>)Bukkit.getServicesManager().getRegistration((Class)Chat.class);
        if (chatProvider != null) {
            this.chat = (Chat)chatProvider.getProvider();
        }
    }
    
    public boolean has(final CommandSender sender, final String node) {
        return this.permission.has(sender, node);
    }
    
    public String getPlayerPrefix(final Player player) {
        return this.chat.getPlayerPrefix(player);
    }
    
    public String getPlayerSuffix(final Player player) {
        return this.chat.getPlayerSuffix(player);
    }
    
    public String getGroupPrefix(final Player player) {
        return this.chat.getGroupPrefix(player.getWorld(), this.permission.getPrimaryGroup(player));
    }
    
    public String getGroupSuffix(final Player player) {
        return this.chat.getGroupSuffix(player.getWorld(), this.permission.getPrimaryGroup(player));
    }
}
