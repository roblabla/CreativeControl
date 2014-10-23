package me.FurH.CreativeControl.configuration;

import me.FurH.CreativeControl.core.configuration.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.core.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.inventory.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class CreativeMainConfig extends Configuration
{
    public String database_type;
    public String database_host;
    public String database_port;
    public String database_user;
    public String database_pass;
    public String database_table;
    public String database_prefix;
    public boolean perm_enabled;
    public boolean perm_keep;
    public String perm_creative;
    public boolean perm_ophas;
    public int queue_threadds;
    public double queue_speed;
    public int cache_capacity;
    public int cache_precache;
    public boolean config_single;
    public boolean config_conflict;
    public boolean config_friend;
    public boolean updater_enabled;
    public boolean selection_usewe;
    public int selection_tool;
    public boolean events_move;
    public boolean events_misc;
    public boolean data_inventory;
    public boolean data_status;
    public boolean data_teleport;
    public boolean data_survival;
    public boolean data_glitch;
    public boolean data_hide;
    public ItemStack armor_helmet;
    public ItemStack armor_chest;
    public ItemStack armor_leggs;
    public ItemStack armor_boots;
    public boolean com_quiet;
    public boolean com_debugcons;
    public boolean com_debugstack;
    
    public CreativeMainConfig(final CorePlugin plugin) {
        super(plugin);
        this.database_type = "SQLite";
        this.database_host = "localhost";
        this.database_port = "3306";
        this.database_user = "root";
        this.database_pass = "123";
        this.database_table = "minecraft";
        this.database_prefix = "crcr_";
        this.perm_enabled = false;
        this.perm_keep = false;
        this.perm_creative = "CreativeGroup";
        this.perm_ophas = true;
        this.queue_threadds = 1;
        this.queue_speed = 0.1;
        this.cache_capacity = 15000;
        this.cache_precache = 10000;
        this.config_single = false;
        this.config_conflict = false;
        this.config_friend = false;
        this.updater_enabled = true;
        this.selection_usewe = false;
        this.selection_tool = 369;
        this.events_move = false;
        this.events_misc = false;
        this.data_inventory = true;
        this.data_status = true;
        this.data_teleport = false;
        this.data_survival = false;
        this.data_glitch = false;
        this.data_hide = false;
        this.armor_helmet = null;
        this.armor_chest = null;
        this.armor_leggs = null;
        this.armor_boots = null;
        this.com_quiet = false;
        this.com_debugcons = false;
        this.com_debugstack = true;
    }
    
    public void load() {
        this.database_type = this.getString("Database.Type");
        this.database_host = this.getString("Database.host");
        this.database_port = this.getString("Database.port");
        this.database_user = this.getString("Database.user");
        this.database_pass = this.getString("Database.pass");
        this.database_table = this.getString("Database.database");
        this.database_prefix = this.getString("Database.prefix");
        this.perm_enabled = this.getBoolean("Permissions.ChangeGroups");
        this.perm_creative = this.getString("Permissions.CreativeGroup");
        this.perm_keep = this.getBoolean("Permissions.KeepCurrentGroup");
        this.perm_ophas = this.getBoolean("Permissions.OpHasPerm");
        this.queue_threadds = this.getInteger("Queue.Threads");
        this.queue_speed = this.getDouble("Queue.Speed");
        this.cache_capacity = this.getInteger("Cache.MaxCapacity");
        this.cache_precache = this.getInteger("Cache.PreCache");
        this.config_single = this.getBoolean("Configurations.Single");
        this.config_conflict = this.getBoolean("Configurations.Conflict");
        this.config_friend = this.getBoolean("Configurations.FriendSystem");
        this.updater_enabled = this.getBoolean("Updater.Enabled");
        this.selection_usewe = this.getBoolean("Selection.UseWorldEdit");
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            this.selection_usewe = false;
        }
        this.selection_tool = this.getInteger("Selection.Tool");
        this.events_move = this.getBoolean("Events.PlayerMove");
        this.events_misc = this.getBoolean("Events.MiscProtection");
        this.data_inventory = this.getBoolean("PlayerData.Inventory");
        this.data_status = this.getBoolean("PlayerData.Status");
        this.data_teleport = this.getBoolean("PlayerData.Teleport");
        this.data_survival = this.getBoolean("PlayerData.SetSurvival");
        this.data_glitch = this.getBoolean("PlayerData.FallGlitch");
        this.data_hide = this.getBoolean("PlayerData.HideInventory");
        try {
            this.armor_helmet = InventoryStack.getItemStackFromString(this.getString("CreativeArmor.Helmet"));
            this.armor_chest = InventoryStack.getItemStackFromString(this.getString("CreativeArmor.Chestplate"));
            this.armor_leggs = InventoryStack.getItemStackFromString(this.getString("CreativeArmor.Leggings"));
            this.armor_boots = InventoryStack.getItemStackFromString(this.getString("CreativeArmor.Boots"));
        }
        catch (CoreException ex) {
            CreativeControl.getPlugin().error(ex, "Failed to handle the creative armor settings!", new Object[0]);
        }
        this.com_quiet = this.getBoolean("Communicator.Quiet");
        this.com_debugcons = this.getBoolean("Debug.Console");
        this.com_debugstack = this.getBoolean("Debug.Stack");
        this.updateConfig();
    }
}
