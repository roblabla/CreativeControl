package me.FurH.CreativeControl.core.perm;

import me.FurH.CreativeControl.core.*;
import org.bukkit.*;
import org.bukkit.plugin.*;

public class CorePermissions
{
    private static ICorePermissions permissions;
    
    public static ICorePermissions getPermissionsBridge(final CorePlugin core) {
        if (CorePermissions.permissions != null) {
            return CorePermissions.permissions;
        }
        final PluginManager pm = Bukkit.getPluginManager();
        final Plugin plugin = pm.getPlugin("Vault");
        if (plugin != null && plugin.isEnabled()) {
            CorePermissions.permissions = new CoreVault();
            core.log("[TAG] Vault hooked as permission plugin!", new Object[0]);
        }
        return CorePermissions.permissions;
    }
}
