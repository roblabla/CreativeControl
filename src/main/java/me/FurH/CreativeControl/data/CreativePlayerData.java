package me.FurH.CreativeControl.data;

import me.FurH.CreativeControl.core.cache.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.database.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.core.util.*;
import java.sql.*;
import me.FurH.CreativeControl.core.inventory.*;

public class CreativePlayerData
{
    public CoreSafeCache<String, CreativePlayerCache> adventurer_cache;
    public CoreSafeCache<String, CreativePlayerCache> creative_cache;
    public CoreSafeCache<String, CreativePlayerCache> survival_cache;
    
    public CreativePlayerData() {
        super();
        this.adventurer_cache = new CoreSafeCache<String, CreativePlayerCache>();
        this.creative_cache = new CoreSafeCache<String, CreativePlayerCache>();
        this.survival_cache = new CoreSafeCache<String, CreativePlayerCache>();
    }
    
    public void clear() {
        this.adventurer_cache.clear();
        this.creative_cache.clear();
        this.survival_cache.clear();
    }
    
    public void clear(final String player) {
        this.adventurer_cache.remove(player);
        this.creative_cache.remove(player);
        this.survival_cache.remove(player);
    }
    
    public boolean process(final Player player, final GameMode newgm, final GameMode oldgm) {
        return this.save(player, oldgm) && this.restore(player, newgm);
    }
    
    public boolean save(final Player p, final GameMode gm) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (gm.equals((Object)GameMode.ADVENTURE)) {
            CreativePlayerCache cache = this.hasAdv(p.getName());
            if (cache == null) {
                cache = new CreativePlayerCache();
                cache.name = p.getName().toLowerCase();
                cache = this.newCache(p, cache);
                this.adventurer_cache.put(cache.name, cache);
                final String query = "INSERT INTO `" + db.prefix + "players_adventurer` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES " + "('" + db.getPlayerId(cache.name) + "', '" + cache.health + "', '" + cache.food + "', '" + cache.ex + "', '" + cache.sat + "', '" + cache.exp + "', '" + this.toArrayString(cache.armor) + "', '" + this.toArrayString(cache.items) + "');";
                try {
                    db.execute(query, new Object[0]);
                }
                catch (CoreException ex) {
                    CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " adventurer data", new Object[0]);
                    return false;
                }
                return true;
            }
            cache = this.newCache(p, cache);
            this.adventurer_cache.remove(cache.name);
            this.adventurer_cache.put(cache.name, cache);
            final String query = "UPDATE `" + db.prefix + "players_adventurer` SET health = '" + cache.health + "', foodlevel = '" + cache.food + "', exhaustion = '" + cache.ex + "', " + "saturation = '" + cache.sat + "', experience = '" + cache.exp + "', armor = '" + this.toArrayString(cache.armor) + "', inventory = '" + this.toArrayString(cache.items) + "' WHERE id = '" + cache.id + "'";
            try {
                db.execute(query, new Object[0]);
            }
            catch (CoreException ex) {
                CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " adventurer data", new Object[0]);
                return false;
            }
            return true;
        }
        else if (gm.equals((Object)GameMode.CREATIVE)) {
            CreativePlayerCache cache = this.hasCre(p.getName());
            if (cache == null) {
                cache = new CreativePlayerCache();
                cache.name = p.getName().toLowerCase();
                cache = this.newCache(p, cache);
                this.creative_cache.put(cache.name, cache);
                final String query = "INSERT INTO `" + db.prefix + "players_creative` (player, armor, inventory) VALUES " + "('" + db.getPlayerId(cache.name) + "', '" + this.toArrayString(cache.armor) + "', '" + this.toArrayString(cache.items) + "');";
                try {
                    db.execute(query, new Object[0]);
                }
                catch (CoreException ex) {
                    CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " creative data", new Object[0]);
                    return false;
                }
                return true;
            }
            cache = this.newCache(p, cache);
            this.creative_cache.remove(cache.name);
            this.creative_cache.put(cache.name, cache);
            final String query = "UPDATE `" + db.prefix + "players_creative` SET armor = '" + this.toArrayString(cache.armor) + "', inventory = '" + this.toArrayString(cache.items) + "' WHERE id = '" + cache.id + "'";
            try {
                db.execute(query, new Object[0]);
            }
            catch (CoreException ex) {
                CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " creative data", new Object[0]);
                return false;
            }
            return true;
        }
        else {
            if (!gm.equals((Object)GameMode.SURVIVAL)) {
                return false;
            }
            CreativePlayerCache cache = this.hasSur(p.getName());
            if (cache == null) {
                cache = new CreativePlayerCache();
                cache.name = p.getName().toLowerCase();
                cache = this.newCache(p, cache);
                this.survival_cache.put(cache.name, cache);
                final String query = "INSERT INTO `" + db.prefix + "players_survival` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES " + "('" + db.getPlayerId(cache.name) + "', '" + cache.health + "', '" + cache.food + "', '" + cache.ex + "', '" + cache.sat + "', '" + cache.exp + "', '" + this.toArrayString(cache.armor) + "', '" + this.toArrayString(cache.items) + "');";
                try {
                    db.execute(query, new Object[0]);
                }
                catch (CoreException ex) {
                    CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " survival data", new Object[0]);
                    return false;
                }
                return true;
            }
            cache = this.newCache(p, cache);
            this.survival_cache.remove(cache.name);
            this.survival_cache.put(cache.name, cache);
            final String query = "UPDATE `" + db.prefix + "players_survival` SET health = '" + cache.health + "', foodlevel = '" + cache.food + "', exhaustion = '" + cache.ex + "', " + "saturation = '" + cache.sat + "', experience = '" + cache.exp + "', armor = '" + this.toArrayString(cache.armor) + "', inventory = '" + this.toArrayString(cache.items) + "' WHERE id = '" + cache.id + "'";
            try {
                db.execute(query, new Object[0]);
            }
            catch (CoreException ex) {
                CreativeControl.plugin.getCommunicator().error(ex, "Failed to save " + p.getName() + " survival data", new Object[0]);
                return false;
            }
            return true;
        }
    }
    
    public CreativePlayerCache newCache(final Player p, final CreativePlayerCache cache) {
        cache.armor = p.getInventory().getArmorContents();
        cache.health = p.getHealth();
        cache.food = p.getFoodLevel();
        cache.ex = p.getExhaustion();
        cache.exp = p.getTotalExperience();
        cache.sat = p.getSaturation();
        cache.items = p.getInventory().getContents();
        return cache;
    }
    
    public boolean restore(final Player p, final GameMode gm) {
        if (gm.equals((Object)GameMode.ADVENTURE)) {
            final CreativePlayerCache cache = this.hasAdv(p.getName());
            return this.restore(p, cache);
        }
        if (gm.equals((Object)GameMode.CREATIVE)) {
            final CreativePlayerCache cache = this.hasCre(p.getName());
            final ItemStack[] armor = this.setCreativeArmor(p);
            if (armor != null && cache != null) {
                cache.armor = armor;
            }
            return this.restore(p, cache);
        }
        if (gm.equals((Object)GameMode.SURVIVAL)) {
            final CreativePlayerCache cache = this.hasSur(p.getName());
            return this.restore(p, cache);
        }
        return false;
    }
    
    private ItemStack[] setCreativeArmor(final Player p) {
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.armor_helmet != null && config.armor_helmet.getType() != Material.AIR) {
            p.getInventory().setHelmet(config.armor_helmet);
        }
        if (config.armor_chest != null && config.armor_chest.getType() != Material.AIR) {
            p.getInventory().setChestplate(config.armor_chest);
        }
        if (config.armor_leggs != null && config.armor_leggs.getType() != Material.AIR) {
            p.getInventory().setLeggings(config.armor_leggs);
        }
        if (config.armor_boots != null && config.armor_boots.getType() != Material.AIR) {
            p.getInventory().setBoots(config.armor_boots);
        }
        return p.getInventory().getArmorContents();
    }
    
    public CreativePlayerCache hasAdv(final String player) {
        CreativePlayerCache cache = this.adventurer_cache.get(player.toLowerCase());
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `" + db.prefix + "players_adventurer` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.health = rs.getDouble("health");
                    cache.food = rs.getInt("foodlevel");
                    cache.ex = rs.getShort("exhaustion");
                    cache.sat = rs.getShort("saturation");
                    cache.exp = rs.getInt("experience");
                    cache.armor = this.toArrayStack(rs.getString("armor"));
                    cache.items = this.toArrayStack(rs.getString("inventory"));
                    this.adventurer_cache.put(cache.name, cache);
                }
            }
            catch (SQLException ex) {
                com.error(ex, "Failed to get " + player + "'s adventurer data", new Object[0]);
            }
            catch (CoreException ex2) {
                com.error(ex2, "Failed to get " + player + "'s adventurer data", new Object[0]);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException ex3) {}
                }
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasSur(final String player) {
        CreativePlayerCache cache = this.survival_cache.get(player.toLowerCase());
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `" + db.prefix + "players_survival` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.health = rs.getDouble("health");
                    cache.food = rs.getInt("foodlevel");
                    cache.ex = rs.getShort("exhaustion");
                    cache.sat = rs.getShort("saturation");
                    cache.exp = rs.getInt("experience");
                    cache.armor = this.toArrayStack(rs.getString("armor"));
                    cache.items = this.toArrayStack(rs.getString("inventory"));
                    this.survival_cache.put(cache.name, cache);
                }
            }
            catch (SQLException ex) {
                com.error(ex, "Failed to get " + player + "'s survival data", new Object[0]);
            }
            catch (CoreException ex2) {
                com.error(ex2, "Failed to get " + player + "'s survival data", new Object[0]);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException ex3) {}
                }
            }
        }
        return cache;
    }
    
    public CreativePlayerCache hasCre(final String player) {
        CreativePlayerCache cache = this.creative_cache.get(player.toLowerCase());
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (cache == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db.getQuery("SELECT * FROM `" + db.prefix + "players_creative` WHERE player = '" + db.getPlayerId(player.toLowerCase()) + "'", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    cache = new CreativePlayerCache();
                    cache.id = rs.getInt("id");
                    cache.name = rs.getString("player");
                    cache.armor = this.toArrayStack(rs.getString("armor"));
                    cache.items = this.toArrayStack(rs.getString("inventory"));
                    this.creative_cache.put(cache.name, cache);
                }
            }
            catch (SQLException ex) {
                com.error(ex, "Failed to get " + player + "'s creative data", new Object[0]);
            }
            catch (CoreException ex2) {
                com.error(ex2, "Failed to get " + player + "'s creative data", new Object[0]);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException ex3) {}
                }
            }
        }
        return cache;
    }
    
    private boolean restore(final Player p, CreativePlayerCache cache) {
        if (cache == null) {
            cache = new CreativePlayerCache();
        }
        p.getInventory().setArmorContents(cache.armor);
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.data_status) {
            double health = cache.health;
            if (health <= 0.0) {
                health = 20.0;
            }
            if (health > 20.0) {
                health = 20.0;
            }
            p.setHealth(health);
            p.setFoodLevel(cache.food);
            p.setExhaustion(cache.ex);
            p.setSaturation(cache.sat);
        }
        p.getInventory().setContents(cache.items);
        p.updateInventory();
        return true;
    }
    
    private String toArrayString(final ItemStack[] armor) {
        try {
            return InventoryStack.getStringFromArray(armor);
        }
        catch (CoreException ex) {
            CreativeControl.getPlugin().error(ex, "Failed to parse ItemStack array into a string", new Object[0]);
            return "";
        }
    }
    
    public ItemStack[] toArrayStack(final String string) {
        try {
            return InventoryStack.getArrayFromString(string);
        }
        catch (CoreException ex) {
            CreativeControl.getPlugin().error(ex, "Failed to parse '" + string + "' into an ItemStack array", new Object[0]);
            return new ItemStack[] { new ItemStack(Material.AIR, 1) };
        }
    }
}
