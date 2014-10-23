package me.FurH.CreativeControl.permissions;

import net.milkbowl.vault.permission.*;
import me.FurH.CreativeControl.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.util.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.configuration.*;

public class CreativePermissions
{
    private Permission vault;
    private CreativePermissionsInterface handler;
    
    public void setup() {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final PluginManager pm = Bukkit.getPluginManager();
        Plugin plugin = pm.getPlugin("Vault");
        if (plugin != null && plugin.isEnabled()) {
            final RegisteredServiceProvider<Permission> permissionProvider = (RegisteredServiceProvider<Permission>)Bukkit.getServicesManager().getRegistration((Class)Permission.class);
            if (permissionProvider != null) {
                this.vault = (Permission)permissionProvider.getProvider();
                com.log("[TAG] Vault hooked as permissions plugin", new Object[0]);
            }
        }
        plugin = pm.getPlugin("GroupManager");
        if (plugin != null && plugin.isEnabled()) {
            this.handler = new CreativeGroupManager(plugin);
            com.log("[TAG] GroupManager hooked as permissions plugin", new Object[0]);
        }
    }
    
    public boolean hasPerm(final Player player, final String node) {
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (player.isOp() && config.perm_ophas) {
            return true;
        }
        if (this.handler != null) {
            return this.handler.hasPerm(player, node);
        }
        return player.hasPermission(node);
    }
    
    public Permission getVault() {
        return this.vault;
    }
}
