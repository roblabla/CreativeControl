package me.FurH.CreativeControl.configuration;

import me.FurH.CreativeControl.core.configuration.*;
import me.FurH.CreativeControl.core.cache.*;
import me.FurH.CreativeControl.core.*;
import me.FurH.CreativeControl.*;
import org.bukkit.*;
import me.FurH.CreativeControl.blacklist.*;

public class CreativeWorldConfig extends Configuration
{
    private CoreSafeCache<String, CreativeWorldNodes> config_cache;
    private CreativeWorldNodes nodes;
    
    public CreativeWorldConfig(final CorePlugin plugin) {
        super(plugin);
        this.config_cache = new CoreSafeCache<String, CreativeWorldNodes>();
        this.nodes = new CreativeWorldNodes();
    }
    
    public void clear() {
        this.config_cache.clear();
    }
    
    public CreativeWorldNodes get(final World w) {
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (main.config_single) {
            return this.nodes;
        }
        final CreativeWorldNodes n = this.config_cache.get(w.getName());
        if (n == null) {
            this.load(w);
        }
        return n;
    }
    
    public void load(final World w) {
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        final CreativeWorldNodes x = new CreativeWorldNodes();
        final String gamemode = this.getString(w, "World.GameMode");
        if (gamemode.equalsIgnoreCase("CREATIVE")) {
            x.world_gamemode = GameMode.CREATIVE;
        }
        else if (gamemode.equalsIgnoreCase("ADVENTURE")) {
            x.world_gamemode = GameMode.ADVENTURE;
        }
        else {
            x.world_gamemode = GameMode.SURVIVAL;
        }
        x.world_exclude = this.getBoolean(w, "World.Exclude");
        x.world_changegm = this.getBoolean(w, "World.ChangeGameMode");
        x.black_cmds = this.getStringAsStringSet(w, "BlackList.Commands");
        x.black_s_cmds = this.getStringAsStringSet(w, "BlackList.SurvivalCommands");
        x.black_place = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlackList.BlockPlace"));
        x.black_break = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlackList.BlockBreak"));
        x.black_use = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlackList.ItemUse"));
        x.black_interact = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlackList.ItemInteract"));
        x.black_inventory = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlackList.Inventory"));
        x.black_sign = this.getStringAsStringSet(w, "BlackList.SignText");
        x.black_sign_all = false;
        x.misc_tnt = this.getBoolean(w, "MiscProtection.NoTNTExplosion");
        x.misc_ice = this.getBoolean(w, "MiscProtection.IceMelt");
        x.misc_liquid = this.getBoolean(w, "MiscProtection.LiquidControl");
        x.misc_fire = this.getBoolean(w, "MiscProtection.Fire");
        x.block_worledit = this.getBoolean(w, "BlockProtection.WorldEdit");
        x.block_ownblock = this.getBoolean(w, "BlockProtection.OwnBlocks");
        x.block_nodrop = this.getBoolean(w, "BlockProtection.NoDrop");
        if (x.block_ownblock && x.block_nodrop) {
            x.block_nodrop = false;
        }
        x.block_water = this.getBoolean(w, "BlockProtection.WaterFlow");
        x.block_explosion = this.getBoolean(w, "BlockProtection.Explosions");
        x.block_creative = this.getBoolean(w, "BlockProtection.CreativeOnly");
        x.block_pistons = this.getBoolean(w, "BlockProtection.Pistons");
        x.block_physics = this.getBoolean(w, "BlockProtection.Physics");
        x.block_against = this.getBoolean(w, "BlockProtection.BlockAgainst");
        x.block_attach = this.getBoolean(w, "BlockProtection.CheckAttached");
        x.block_invert = this.getBoolean(w, "BlockProtection.inverted");
        x.block_exclude = blacklist.buildHashSet(this.getStringAsStringSet(w, "BlockProtection.exclude"));
        x.block_minutelimit = this.getInteger(w, "BlockProtection.BlockPerMinute");
        x.prevent_drop = this.getBoolean(w, "Preventions.ItemDrop");
        x.prevent_pickup = this.getBoolean(w, "Preventions.ItemPickup");
        x.prevent_pvp = this.getBoolean(w, "Preventions.PvP");
        x.prevent_mobs = this.getBoolean(w, "Preventions.Mobs");
        x.prevent_eggs = this.getBoolean(w, "Preventions.Eggs");
        x.prevent_target = this.getBoolean(w, "Preventions.Target");
        x.prevent_mobsdrop = this.getBoolean(w, "Preventions.MobsDrop");
        x.prevent_irongolem = this.getBoolean(w, "Preventions.IronGolem");
        x.prevent_snowgolem = this.getBoolean(w, "Preventions.SnowGolem");
        x.prevent_wither = this.getBoolean(w, "Preventions.Wither");
        x.prevent_drops = this.getBoolean(w, "Preventions.ClearDrops");
        x.prevent_enchant = this.getBoolean(w, "Preventions.Enchantments");
        x.prevent_mcstore = this.getBoolean(w, "Preventions.MineCartStorage");
        x.prevent_bedrock = this.getBoolean(w, "Preventions.BreakBedRock");
        x.prevent_invinteract = this.getBoolean(w, "Preventions.InvInteract");
        x.prevent_bonemeal = this.getBoolean(w, "Preventions.Bonemeal");
        x.prevent_villager = this.getBoolean(w, "Preventions.InteractVillagers");
        x.prevent_potion = this.getBoolean(w, "Preventions.PotionSplash");
        x.prevent_frame = this.getBoolean(w, "Preventions.ItemFrame");
        x.prevent_vehicle = this.getBoolean(w, "Preventions.VehicleDrop");
        x.prevent_limitvechile = this.getInteger(w, "Preventions.VehicleLimit");
        x.prevent_stacklimit = this.getInteger(w, "Preventions.StackLimit");
        x.prevent_open = this.getBoolean(w, "Preventions.InventoryOpen");
        x.prevent_fly = this.getBoolean(w, "Preventions.RemoveFlyOnPvP");
        x.prevent_creative = this.getBoolean(w, "Preventions.NoCreativeOnPvP");
        if (!main.config_single) {
            this.config_cache.put(w.getName(), x);
        }
        else {
            this.nodes = x;
        }
        this.updateConfig();
    }
}
