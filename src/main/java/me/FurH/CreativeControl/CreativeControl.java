package me.FurH.CreativeControl;

import me.FurH.CreativeControl.core.*;
import me.FurH.CreativeControl.database.*;
import me.FurH.CreativeControl.selection.*;
import me.FurH.CreativeControl.manager.*;
import me.FurH.CreativeControl.data.friend.*;
import me.FurH.CreativeControl.blacklist.*;
import me.FurH.CreativeControl.permissions.*;
import me.FurH.CreativeControl.core.updater.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.listener.*;
import me.FurH.CreativeControl.core.internals.*;
import me.FurH.CreativeControl.commands.*;
import me.FurH.CreativeControl.data.*;
import me.FurH.CreativeControl.database.extra.*;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.integration.*;
import java.util.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.configuration.*;
import de.diddiz.LogBlock.*;
import net.coreprotect.*;
import me.FurH.CreativeControl.integration.worldedit.*;
import com.sk89q.worldedit.bukkit.*;
import me.FurH.CreativeControl.metrics.*;
import me.FurH.CreativeControl.region.*;
import org.bukkit.*;
import java.io.*;

public class CreativeControl extends CorePlugin
{
    public static CreativeControl plugin;
    private static CreativeSQLDatabase database;
    private static CreativeBlocksSelection selector;
    private static CreativeRegionManager regioner;
    private static CreativeBlockManager manager;
    private static CreativePlayerData data;
    private static CreativePlayerFriends friends;
    private static CreativeMainConfig mainconfig;
    private static CreativeMessages messages;
    private static Consumer lbconsumer;
    private static CreativeWorldConfig worldconfig;
    private static boolean prismEnabled;
    private static CoreProtectAPI coreprotect;
    private static CreativeBlackList blacklist;
    private static CreativePermissions permissions;
    public WeakHashMap<Player, Location> right;
    public WeakHashMap<Player, Location> left;
    public Map<String, Integer> mods;
    public Map<String, HashSet<UUID>> limits;
    public CoreUpdater updater;
    private int survival;
    private int creative;
    private int useMove;
    private int useMisc;
    private int OwnBlock;
    private int NoDrop;
    
    public CreativeControl() {
        super("&8[&3CreativeControl&8]&7:&f");
        this.right = new WeakHashMap<Player, Location>();
        this.left = new WeakHashMap<Player, Location>();
        this.mods = new HashMap<String, Integer>();
        this.limits = new HashMap<String, HashSet<UUID>>();
        this.survival = 0;
        this.creative = 0;
        this.useMove = 0;
        this.useMisc = 0;
        this.OwnBlock = 0;
        this.NoDrop = 0;
    }
    
    public void onEnable() {
        final long start = System.currentTimeMillis();
        CreativeControl.plugin = this;
        this.updater = new CoreUpdater(this, "http://dev.bukkit.org/server-mods/creativecontrol/");
        (CreativeControl.messages = new CreativeMessages(this)).load();
        this.getCommunicator().setTag(CreativeControl.messages.prefix_tag);
        CreativeControl.blacklist = new CreativeBlackList();
        this.log("[TAG] Initializing configurations...", new Object[0]);
        (CreativeControl.mainconfig = new CreativeMainConfig(this)).load();
        (CreativeControl.worldconfig = new CreativeWorldConfig(this)).setSingleConfig(CreativeControl.mainconfig.config_single);
        if (!CreativeControl.mainconfig.config_single) {
            for (final World w : this.getServer().getWorlds()) {
                CreativeControl.worldconfig.load(w);
            }
        }
        else {
            CreativeControl.worldconfig.load(this.getServer().getWorlds().get(0));
        }
        CreativeControl.mainconfig.updateConfig();
        this.getCommunicator().setDebug(CreativeControl.mainconfig.com_debugcons);
        this.getCommunicator().setQuiet(CreativeControl.mainconfig.com_quiet);
        this.log("[TAG] Loading Modules...", new Object[0]);
        CreativeControl.selector = new CreativeBlocksSelection();
        CreativeControl.regioner = new CreativeRegionManager();
        CreativeControl.manager = new CreativeBlockManager();
        CreativeControl.friends = new CreativePlayerFriends();
        CreativeControl.data = new CreativePlayerData();
        CreativeControl.permissions = new CreativePermissions();
        (CreativeControl.database = new CreativeSQLDatabase(this, CreativeControl.mainconfig.database_prefix, CreativeControl.mainconfig.database_type, CreativeControl.mainconfig.database_host, CreativeControl.mainconfig.database_port, CreativeControl.mainconfig.database_table, CreativeControl.mainconfig.database_user, CreativeControl.mainconfig.database_pass)).setupQueue(CreativeControl.mainconfig.queue_speed, CreativeControl.mainconfig.queue_threadds);
        CreativeControl.database.setAllowMainThread(true);
        try {
            CreativeControl.database.setAutoCommit(false);
        }
        catch (CoreException ex) {
            this.error(ex);
        }
        try {
            CreativeControl.database.connect();
        }
        catch (CoreException ex) {
            this.error(ex);
        }
        CreativeControl.database.load();
        try {
            CreativeControl.database.commit();
        }
        catch (CoreException ex) {
            ex.printStackTrace();
        }
        this.log("[TAG] Registring Events...", new Object[0]);
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)new CreativeBlockListener(), (Plugin)this);
        pm.registerEvents((Listener)new CreativeEntityListener(), (Plugin)this);
        pm.registerEvents((Listener)new CreativePlayerListener(), (Plugin)this);
        pm.registerEvents((Listener)new CreativeWorldListener(), (Plugin)this);
        if (CreativeControl.mainconfig.events_move) {
            pm.registerEvents((Listener)new CreativeMoveListener(), (Plugin)this);
        }
        if (CreativeControl.mainconfig.events_misc) {
            pm.registerEvents((Listener)new CreativeMiscListener(), (Plugin)this);
        }
        if (CreativeControl.mainconfig.data_hide && pm.getPlugin("AuthMe") != null) {
            InternalManager.setup(true);
            pm.registerEvents((Listener)new CreativeHideInventory(), (Plugin)this);
            pm.registerEvents((Listener)new AuthMe(), (Plugin)this);
        }
        this.loadIntegrations();
        final CommandExecutor cc = (CommandExecutor)new CreativeCommands();
        this.getCommand("terraminingcontrol").setExecutor(cc);
        this.setupWorldEdit();
        this.setupLoggers();
        CreativeControl.permissions.setup();
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                CreativeControl.this.log("[TAG] Cached {0} protections", CreativeControl.manager.preCache());
                CreativeControl.this.log("[TAG] Loaded {0} regions", CreativeControl.regioner.loadRegions());
                CreativeControl.this.log("[TAG] {0} blocks protected", CreativeControl.manager.getTotal());
            }
        });
        if (CreativeControl.mainconfig.updater_enabled) {
            this.updater.setup();
        }
        this.startMetrics();
        try {
            if (CreativeControl.database.isUpdateAvailable()) {
                this.log("[TAG] Database update required!", new Object[0]);
                if (CreativeControl.database.getCurrentVersion() >= 2) {
                    final CreativeDataUpdater invUpdater = new CreativeDataUpdater(this);
                    invUpdater.run();
                }
                else {
                    Bukkit.getScheduler().runTaskAsynchronously((Plugin)this, (Runnable)new CreativeSQLUpdater(this));
                }
            }
        }
        catch (CoreException ex2) {
            this.error(ex2);
        }
        this.logEnable(Math.abs(System.currentTimeMillis() - start));
    }
    
    public void onDisable() {
        final long start = System.currentTimeMillis();
        try {
            CreativeControl.database.disconnect(false);
        }
        catch (CoreException ex) {
            this.error(ex);
        }
        HandlerList.unregisterAll((Plugin)this);
        this.getServer().getScheduler().cancelTasks((Plugin)this);
        this.clear();
        this.right.clear();
        this.left.clear();
        this.mods.clear();
        CreativeControl.data.clear();
        CreativeControl.friends.clear();
        this.limits.clear();
        CreativeControl.messages.unload();
        CreativeControl.mainconfig.unload();
        CreativeControl.worldconfig.unload();
        CreativeControl.worldconfig.clear();
        CreativeControl.plugin = null;
        CreativeControl.database = null;
        CreativeControl.selector = null;
        CreativeControl.regioner = null;
        CreativeControl.manager = null;
        CreativeControl.data = null;
        CreativeControl.friends = null;
        CreativeControl.mainconfig = null;
        CreativeControl.messages = null;
        CreativeControl.lbconsumer = null;
        CreativeControl.worldconfig = null;
        CreativeControl.prismEnabled = false;
        CreativeControl.coreprotect = null;
        CreativeControl.blacklist = null;
        CreativeControl.permissions = null;
        this.updater = null;
        this.logDisable(Math.abs(System.currentTimeMillis() - start));
    }
    
    public void reload(final CommandSender sender) {
        final String ssql = CreativeControl.mainconfig.database_type;
        final boolean move = CreativeControl.mainconfig.events_move;
        final boolean misc = CreativeControl.mainconfig.events_misc;
        this.clear();
        this.right.clear();
        this.left.clear();
        this.mods.clear();
        CreativeControl.data.clear();
        CreativeControl.friends.clear();
        this.limits.clear();
        CreativeControl.messages.unload();
        CreativeControl.mainconfig.unload();
        CreativeControl.worldconfig.unload();
        CreativeControl.messages.load();
        CreativeControl.mainconfig.load();
        CreativeControl.worldconfig.clear();
        if (!CreativeControl.mainconfig.config_single) {
            for (final World w : this.getServer().getWorlds()) {
                CreativeControl.worldconfig.load(w);
            }
        }
        else {
            CreativeControl.worldconfig.load(this.getServer().getWorlds().get(0));
        }
        this.loadIntegrations();
        final String newssql = CreativeControl.mainconfig.database_type;
        final boolean newmove = CreativeControl.mainconfig.events_move;
        final boolean newmisc = CreativeControl.mainconfig.events_misc;
        if (!ssql.equals(newssql)) {
            try {
                CreativeControl.database.disconnect(false);
            }
            catch (CoreException ex) {
                this.error(ex);
            }
            try {
                CreativeControl.database.connect();
            }
            catch (CoreException ex) {
                this.error(ex);
            }
            CreativeControl.database.load();
            this.msg(sender, "[TAG] Database Type: &4{0}&7 Defined.", CreativeControl.database.getDatabaseEngine());
        }
        final PluginManager pm = this.getServer().getPluginManager();
        if (move != newmove) {
            if (newmove) {
                pm.registerEvents((Listener)new CreativeMoveListener(), (Plugin)this);
                this.msg(sender, "[TAG] CreativeMoveListener registred, Listener enabled.", new Object[0]);
            }
            else {
                HandlerList.unregisterAll((Listener)new CreativeMoveListener());
                this.msg(sender, "[TAG] CreativeMoveListener unregistered, Listener disabled.", new Object[0]);
            }
        }
        if (misc != newmisc) {
            if (newmisc) {
                pm.registerEvents((Listener)new CreativeMiscListener(), (Plugin)this);
                this.msg(sender, "[TAG] CreativeMiscListener registred, Listener enabled.", new Object[0]);
            }
            else {
                HandlerList.unregisterAll((Listener)new CreativeMoveListener());
                this.msg(sender, "[TAG] CreativeMiscListener unregistered, Listener disabled.", new Object[0]);
            }
        }
    }
    
    public void loadIntegrations() {
        final PluginManager pm = this.getServer().getPluginManager();
        Plugin p = pm.getPlugin("MobArena");
        if (p != null && p.isEnabled()) {
            this.log("[TAG] MobArena support enabled!", new Object[0]);
            pm.registerEvents((Listener)new MobArena(), (Plugin)this);
        }
        try {
            Class.forName("org.mcsg.survivalgames.api.PlayerJoinArenaEvent");
            p = pm.getPlugin("SurvivalGames");
            if (p != null && p.isEnabled()) {
                this.log("[TAG] SurvivalGames support enabled!", new Object[0]);
                pm.registerEvents((Listener)new SurvivalGames(), (Plugin)this);
            }
        }
        catch (Exception ex) {}
        p = pm.getPlugin("Multiverse-Inventories");
        if (p != null && p.isEnabled() && (CreativeControl.mainconfig.data_inventory || CreativeControl.mainconfig.data_status)) {
            if (CreativeControl.mainconfig.config_conflict) {
                CreativeControl.mainconfig.data_inventory = false;
                CreativeControl.mainconfig.data_status = false;
                for (int anoy = 5; anoy > 0; --anoy) {
                    this.log("[TAG] ***************************************************", new Object[0]);
                    this.log("[TAG] Multiverse-Inventories Detected!!", new Object[0]);
                    this.log("[TAG] Per-GameMode inventories will be disabled by this plugin", new Object[0]);
                    this.log("[TAG] Use the multiverse inventories manager!", new Object[0]);
                    this.log("[TAG] ***************************************************", new Object[0]);
                }
            }
            else {
                this.log("[TAG] ***************************************************", new Object[0]);
                this.log("[TAG] Multiverse-Inventories Detected!!", new Object[0]);
                this.log("[TAG] Per-GameMode inventories may be buggy!", new Object[0]);
                this.log("[TAG] Use the multiverse inventories manager!", new Object[0]);
                this.log("[TAG] ***************************************************", new Object[0]);
            }
        }
    }
    
    @Override
    public boolean hasPerm(final CommandSender sender, final String node) {
        return !(sender instanceof Player) || CreativeControl.permissions.hasPerm((Player)sender, "CreativeControl." + node);
    }
    
    public String removeVehicle(final UUID uuid) {
        String master = null;
        for (final String key : this.limits.keySet()) {
            if (this.limits.get(key).contains(uuid)) {
                master = key;
                break;
            }
        }
        if (master == null) {
            return null;
        }
        final HashSet<UUID> entity = this.limits.get(master);
        entity.remove(uuid);
        this.limits.put(master, entity);
        return master;
    }
    
    private void clear() {
        final HashSet<UUID> entity = new HashSet<UUID>();
        for (final String key : this.limits.keySet()) {
            entity.addAll(this.limits.get(key));
        }
        for (final World w : this.getServer().getWorlds()) {
            for (final Entity x : w.getEntities()) {
                if (entity.contains(x.getUniqueId())) {
                    x.remove();
                }
            }
        }
        entity.clear();
        this.limits.clear();
    }
    
    public void clear(final Player player) {
        final HashSet<UUID> entity = this.limits.get(player.getName());
        if (entity == null) {
            return;
        }
        for (final World w : Bukkit.getWorlds()) {
            for (final Entity x : w.getEntities()) {
                if (entity.contains(x.getUniqueId())) {
                    x.remove();
                }
            }
        }
        entity.clear();
        this.limits.remove(player.getName());
    }
    
    public static CreativeBlackList getBlackList() {
        return CreativeControl.blacklist;
    }
    
    public static CreativePermissions getPermissions2() {
        return CreativeControl.permissions;
    }
    
    public static CreativeWorldConfig getWorldConfig() {
        return CreativeControl.worldconfig;
    }
    
    public static CreativeWorldNodes getWorldNodes(final World world) {
        return CreativeControl.worldconfig.get(world);
    }
    
    public static CreativeControl getPlugin() {
        return CreativeControl.plugin;
    }
    
    public static CreativeBlocksSelection getSelector() {
        return CreativeControl.selector;
    }
    
    public static CreativePlayerFriends getFriends() {
        return CreativeControl.friends;
    }
    
    public static CreativeSQLDatabase getDb() {
        return CreativeControl.database;
    }
    
    public static CreativeRegionManager getRegioner() {
        return CreativeControl.regioner;
    }
    
    public static CreativeMainConfig getMainConfig() {
        return CreativeControl.mainconfig;
    }
    
    public static CreativeBlockManager getManager() {
        return CreativeControl.manager;
    }
    
    public static CreativePlayerData getPlayerData() {
        return CreativeControl.data;
    }
    
    public static CreativeMessages getMessages() {
        return CreativeControl.messages;
    }
    
    public static Consumer getLogBlock() {
        return CreativeControl.lbconsumer;
    }
    
    public static CoreProtectAPI getCoreProtect() {
        return CreativeControl.coreprotect;
    }
    
    public static boolean getPrism() {
        return CreativeControl.prismEnabled;
    }
    
    public void setupLoggers() {
        final Plugin logblock = Bukkit.getPluginManager().getPlugin("LogBlock");
        if (logblock != null) {
            this.log("[TAG] LogBlock hooked as logging plugin", new Object[0]);
            CreativeControl.lbconsumer = ((LogBlock)logblock).getConsumer();
        }
        final Plugin prism = Bukkit.getPluginManager().getPlugin("Prism");
        if (prism != null) {
            this.log("[TAG] Prism hooked as logging plugin", new Object[0]);
            CreativeControl.prismEnabled = true;
        }
        final Plugin corep = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (corep != null) {
            this.log("[TAG] CoreProtect hooked as logging plugin", new Object[0]);
            CreativeControl.coreprotect = ((CoreProtect)corep).getAPI();
        }
    }
    
    public void setupWorldEdit() {
        final Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (we != null && we.isEnabled()) {
            CreativeEditSessionFactory.setup();
        }
    }
    
    public WorldEditPlugin getWorldEdit() {
        final PluginManager pm = this.getServer().getPluginManager();
        final Plugin wex = pm.getPlugin("WorldEdit");
        return (WorldEditPlugin)wex;
    }
    
    private void startMetrics() {
        try {
            final CreativeMetrics metrics = new CreativeMetrics((Plugin)this);
            final CreativeMetrics.Graph dbType = metrics.createGraph("Database Type");
            dbType.addPlotter(new CreativeMetrics.Plotter(CreativeControl.database.getDatabaseEngine().toString()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
            for (final CreativeRegion CR : CreativeControl.regioner.getAreas()) {
                if (CR.gamemode == GameMode.CREATIVE) {
                    ++this.creative;
                }
                else {
                    ++this.survival;
                }
            }
            final CreativeMetrics.Graph reg = metrics.createGraph("Regions");
            reg.addPlotter(new CreativeMetrics.Plotter("Regions") {
                @Override
                public int getValue() {
                    return CreativeControl.this.creative + CreativeControl.this.survival;
                }
            });
            final CreativeMetrics.Graph reg2 = metrics.createGraph("Regions Type");
            reg2.addPlotter(new CreativeMetrics.Plotter("Creative") {
                @Override
                public int getValue() {
                    return CreativeControl.this.creative;
                }
            });
            reg2.addPlotter(new CreativeMetrics.Plotter("Survival") {
                @Override
                public int getValue() {
                    return CreativeControl.this.survival;
                }
            });
            if (CreativeControl.mainconfig.events_move) {
                ++this.useMove;
            }
            if (CreativeControl.mainconfig.events_misc) {
                ++this.useMisc;
            }
            final CreativeMetrics.Graph extra = metrics.createGraph("Extra Events");
            extra.addPlotter(new CreativeMetrics.Plotter("Move Event") {
                @Override
                public int getValue() {
                    return CreativeControl.this.useMove;
                }
            });
            extra.addPlotter(new CreativeMetrics.Plotter("Misc Protection") {
                @Override
                public int getValue() {
                    return CreativeControl.this.useMisc;
                }
            });
            for (final World world : this.getServer().getWorlds()) {
                if (CreativeControl.worldconfig.get(world).block_ownblock) {
                    ++this.OwnBlock;
                }
                else {
                    if (!CreativeControl.worldconfig.get(world).block_nodrop) {
                        continue;
                    }
                    ++this.NoDrop;
                }
            }
            final CreativeMetrics.Graph ptype = metrics.createGraph("Protection Type");
            ptype.addPlotter(new CreativeMetrics.Plotter("OwnBlocks") {
                @Override
                public int getValue() {
                    return CreativeControl.this.OwnBlock;
                }
            });
            ptype.addPlotter(new CreativeMetrics.Plotter("NoDrop") {
                @Override
                public int getValue() {
                    return CreativeControl.this.NoDrop;
                }
            });
            metrics.start();
        }
        catch (IOException ex) {}
    }
    
    static {
        CreativeControl.lbconsumer = null;
    }
}
