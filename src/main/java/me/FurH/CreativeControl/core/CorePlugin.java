package me.FurH.CreativeControl.core;

import org.bukkit.plugin.java.*;
import me.FurH.CreativeControl.core.database.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.core.perm.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.exceptions.*;

public abstract class CorePlugin extends JavaPlugin
{
    public CoreSQLDatabase coredatabase;
    public static Thread main_thread;
    private static ICorePermissions permissions;
    private Communicator communicator;
    private static CorePlugin coreOplugin;
    private boolean registred;
    private boolean outbound;
    private boolean inbound;
    
    public CorePlugin(final String tag) {
        super();
        this.registred = false;
        this.outbound = false;
        this.setup(tag, this.inbound = false, false);
    }
    
    public CorePlugin(final String tag, final boolean inbound) {
        super();
        this.registred = false;
        this.outbound = false;
        this.setup(tag, inbound, this.inbound = false);
    }
    
    public CorePlugin(final String tag, final boolean inbound, final boolean outbound) {
        super();
        this.registred = false;
        this.outbound = false;
        this.inbound = false;
        this.setup(tag, inbound, outbound);
    }
    
    private void setup(final String tag, final boolean inbound, final boolean outbound) {
        CorePlugin.coreOplugin = this;
        this.communicator = new Communicator(CorePlugin.coreOplugin, tag);
        if (Core.start == 0L) {
            Core.start = System.currentTimeMillis();
        }
        if (CorePlugin.main_thread == null) {
            CorePlugin.main_thread = Thread.currentThread();
        }
        if (CorePlugin.permissions == null) {
            CorePlugin.permissions = CorePermissions.getPermissionsBridge(this);
        }
        this.inbound = inbound;
        this.outbound = outbound;
    }
    
    private void registerEvents() {
        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents((Listener)new CoreListener(this.inbound, this.outbound), (Plugin)CorePlugin.coreOplugin);
    }
    
    public static boolean hasPermS(final CommandSender sender, final String node) {
        if (CorePlugin.permissions == null) {
            return sender.hasPermission(node);
        }
        return CorePlugin.permissions.has(sender, node);
    }
    
    public boolean hasPerm(final CommandSender sender, final String node) {
        return hasPermS(sender, node);
    }
    
    public void msg(final CommandSender sender, final String message, final Object... objects) {
        this.communicator.msg(sender, message, objects);
    }
    
    public void logEnable(final long took) {
        if (!this.registred) {
            this.registerEvents();
            this.registred = true;
        }
        this.log("[TAG] {0} v{1} loaded in {2} ms!", this.getDescription().getName(), this.getDescription().getVersion(), took);
    }
    
    public void logEnable() {
        if (!this.registred) {
            this.registerEvents();
            this.registred = true;
        }
        this.log("[TAG] {0} v{1} loaded!", this.getDescription().getName(), this.getDescription().getVersion());
    }
    
    public void logDisable(final long took) {
        this.log("[TAG] {0} v{1} disabled in {2} ms!", this.getDescription().getName(), this.getDescription().getVersion(), took);
    }
    
    public void logDisable() {
        this.log("[TAG] {0} v{1} disabled!", this.getDescription().getName(), this.getDescription().getVersion());
    }
    
    public void log(final String message, final Object... objects) {
        this.communicator.log(message, objects);
    }
    
    public void error(final CoreException ex) {
        this.communicator.error(ex);
    }
    
    public void error(final CoreException ex, final String message, final Object... objects) {
        this.communicator.error(ex, message, objects);
    }
    
    public Communicator getCommunicator() {
        return this.communicator;
    }
    
    public static ICorePermissions getPermissions() {
        return CorePlugin.permissions;
    }
    
    public static CorePlugin getCorePlugin() {
        return CorePlugin.coreOplugin;
    }
}
