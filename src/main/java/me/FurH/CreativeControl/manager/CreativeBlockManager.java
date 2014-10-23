package me.FurH.CreativeControl.manager;

import me.FurH.CreativeControl.core.cache.soft.*;
import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.data.friend.*;
import org.bukkit.block.*;
import me.FurH.CreativeControl.core.location.*;
import me.FurH.CreativeControl.core.list.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.database.*;
import me.FurH.CreativeControl.core.util.*;
import java.util.*;
import java.sql.*;
import org.bukkit.*;
import me.FurH.CreativeControl.stack.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.blacklist.*;

public class CreativeBlockManager
{
    private static CoreSoftCache<String, CreativeBlockData> cache;
    
    public CreativeBlockManager() {
        super();
        (CreativeBlockManager.cache = new CoreSoftCache<String, CreativeBlockData>(CreativeControl.getMainConfig().cache_capacity)).cleanupTask(300000L);
    }
    
    public CoreSoftCache<String, CreativeBlockData> getCache() {
        return CreativeBlockManager.cache;
    }
    
    public boolean isAllowed(final Player p, final CreativeBlockData data) {
        if (data == null) {
            return true;
        }
        if (data.owner != null && data.owner.equalsIgnoreCase(p.getName())) {
            return true;
        }
        if (CreativeControl.plugin.hasPerm((CommandSender)p, "OwnBlock.Bypass")) {
            return true;
        }
        if (data.allowed != null && data.allowed.contains(p.getName())) {
            return true;
        }
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (config.config_friend) {
            final CreativePlayerFriends friends = CreativeControl.getFriends();
            return friends.getFriends(data.owner).contains(p.getName());
        }
        return false;
    }
    
    public void unprotect(final Block b) {
        this.unprotect(b.getWorld(), b.getX(), b.getY(), b.getZ(), b.getTypeId());
    }
    
    public void unprotect(final World world, final int x, final int y, final int z, final int type) {
        if (this.isprotectable(world, type)) {
            CreativeBlockManager.cache.remove(LocationUtils.locationToString(x, y, z, world.getName()));
            CreativeControl.getDb().unprotect(world.getName(), x, y, z);
        }
    }
    
    public void protect(final Player p, final Block b) {
        this.protect(p.getName(), b);
    }
    
    public void protect(final String player, final Block b) {
        this.protect(player, b.getWorld(), b.getX(), b.getY(), b.getZ(), b.getTypeId());
    }
    
    public void protect(final String owner, final World world, final int x, final int y, final int z, final int type) {
        if (this.isprotectable(world, type)) {
            final CreativeBlockData data = new CreativeBlockData(owner, type, null);
            CreativeBlockManager.cache.put(LocationUtils.locationToString(x, y, z, world.getName()), data);
            CreativeControl.getDb().protect(owner, world.getName(), x, y, z, type);
        }
    }
    
    public int preCache() {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        final int worlds = Bukkit.getWorlds().size();
        int pass = 0;
        int count = 0;
        int each = (int)Math.floor(config.cache_precache / worlds);
        try {
            final List<World> worldsx = new ArrayList<World>(Bukkit.getWorlds());
            Collections.reverse(worldsx);
            for (final World world : worldsx) {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + db.prefix + "blocks_" + world.getName() + "` ORDER BY 'time' DESC LIMIT " + each + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                ++pass;
                final boolean nodrop = CreativeControl.getWorldNodes(world).block_nodrop;
                int ran = 0;
                while (rs.next()) {
                    CreativeBlockData data = null;
                    if (!nodrop) {
                        data = new CreativeBlockData(db.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                    }
                    else {
                        data = new CreativeBlockData(rs.getInt("type"));
                    }
                    ++count;
                    CreativeBlockManager.cache.put(LocationUtils.locationToString(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), world.getName()), data);
                    ++ran;
                }
                if (++ran < each) {
                    if (worlds - pass > 0) {
                        each = (int)Math.floor((config.cache_precache - pass) / (worlds - pass));
                    }
                    else {
                        each = config.cache_precache - pass;
                    }
                }
                rs.close();
                ps.close();
            }
            worldsx.clear();
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to add protections to cache", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to add protections to cache", new Object[0]);
        }
        return count;
    }
    
    public int getTotal() {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final Communicator com = CreativeControl.plugin.getCommunicator();
        int total = 0;
        final List<World> worlds = new ArrayList<World>(Bukkit.getWorlds());
        for (final World world : worlds) {
            try {
                total += (int)db.getTableCount(db.prefix + "blocks_" + world.getName());
            }
            catch (CoreException ex) {
                com.error(ex, "Failed to count world tables size", new Object[0]);
            }
        }
        return total;
    }
    
    public void update(final CreativeBlockData data, final Block block) {
        this.update(data, block.getWorld(), block.getX(), block.getY(), block.getZ());
    }
    
    public void update(final CreativeBlockData data, final World world, final int x, final int y, final int z) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (data == null) {
            return;
        }
        if (data.allowed == null || data.allowed.isEmpty()) {
            data.allowed = null;
        }
        db.update(data, world.getName(), x, y, z);
        CreativeBlockManager.cache.put(LocationUtils.locationToString(x, y, z, world.getName()), data);
    }
    
    public CreativeBlockData isprotected(final Block block, final boolean nodrop) {
        return this.isprotected(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getTypeId(), nodrop);
    }
    
    public CreativeBlockData isprotected(final World world, final int x, final int y, final int z, final int type, final boolean nodrop) {
        if (!this.isprotectable(world, type)) {
            return null;
        }
        final String key = LocationUtils.locationToString(x, y, z, world.getName());
        CreativeBlockData data = CreativeBlockManager.cache.get(key);
        if (data != null) {
            return data;
        }
        data = CreativeControl.getDb().isprotected(world.getName(), x, y, z, type, nodrop);
        if (data != null) {
            CreativeBlockManager.cache.put(key, data);
        }
        return data;
    }
    
    public CreativeBlockData getFullData(final Location location) {
        return CreativeControl.getDb().getFullData(location);
    }
    
    public boolean isprotectable(final World world, final int typeId) {
        final CreativeWorldNodes nodes = CreativeControl.getWorldNodes(world);
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        final CreativeItemStack itemStack = new CreativeItemStack(typeId, -1);
        if (nodes.block_invert) {
            return blacklist.isBlackListed(nodes.block_exclude, itemStack);
        }
        return !blacklist.isBlackListed(nodes.block_exclude, itemStack);
    }
    
    public void clear() {
        CreativeBlockManager.cache.clear();
    }
    
    public double getTablesSize() {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        double ret = 0.0;
        for (final World world : Bukkit.getWorlds()) {
            try {
                ret += db.getTableSize(db.prefix + "blocks_" + world.getName());
            }
            catch (CoreException ex) {
                CreativeControl.getPlugin().getCommunicator().error(ex, "Failed to get world tables size", new Object[0]);
            }
        }
        return ret;
    }
    
    public double getTablesFree() {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        double ret = 0.0;
        for (final World world : Bukkit.getWorlds()) {
            try {
                ret += db.getTableFree(db.prefix + "blocks_" + world.getName());
            }
            catch (CoreException ex) {
                CreativeControl.getPlugin().getCommunicator().error(ex, "Failed to get world tables free size", new Object[0]);
            }
        }
        return ret;
    }
}
