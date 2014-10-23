package me.FurH.CreativeControl.database;

import me.FurH.CreativeControl.core.database.*;
import me.FurH.CreativeControl.core.cache.*;
import me.FurH.CreativeControl.core.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.list.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.block.*;
import me.FurH.CreativeControl.manager.*;
import me.FurH.CreativeControl.database.extra.*;
import me.FurH.CreativeControl.core.location.*;
import java.sql.*;
import org.bukkit.*;
import java.util.*;

public final class CreativeSQLDatabase extends CoreSQLDatabase
{
    private static CoreSafeCache<String, Integer> owners;
    
    public CreativeSQLDatabase(final CorePlugin plugin, final String prefix, final String engine, final String database_host, final String database_port, final String database_table, final String database_user, final String database_pass) {
        super(plugin, prefix, engine, database_host, database_port, database_table, database_user, database_pass);
        super.setDatabaseVersion(3);
        this.prefix = prefix;
    }
    
    public String[] getOldGroup(final Player player) {
        String[] ret = null;
        final Communicator com = CreativeControl.plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.getQuery("SELECT groups FROM `" + this.prefix + "groups` WHERE player = '" + this.getPlayerId(player.getName()) + "' LIMIT 1;", new Object[0]);
            rs = ps.getResultSet();
            if (rs.next()) {
                ret = CollectionUtils.toStringList(rs.getString("groups"), ", ").toArray(new String[0]);
            }
            else {
                this.setOldGroups(player);
            }
        }
        catch (Exception ex) {
            com.error(ex, "Failed to get old group data for the player: " + player.getName(), new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(ps);
            CoreSQLDatabase.closeQuietly(rs);
        }
        return ret;
    }
    
    public void saveOldGroups(final Player player, final String[] groups) {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        try {
            this.execute("UPDATE `" + this.prefix + "groups` SET groups = '" + Arrays.toString(groups) + "' WHERE player = '" + this.getPlayerId(player.getName()) + "';", new Object[0]);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to save '" + player.getName() + "' groups data", new Object[0]);
        }
        try {
            this.commit();
        }
        catch (CoreException ex2) {}
    }
    
    private void setOldGroups(final Player player) {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        try {
            this.execute("INSERT INTO `" + this.prefix + "groups` (player, groups) VALUES ('" + this.getPlayerId(player.getName()) + "', '');", new Object[0]);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to save '" + player.getName() + "' groups data", new Object[0]);
        }
        try {
            this.commit();
        }
        catch (CoreException ex2) {}
    }
    
    public void protect(final Player player, final Block block) {
        this.protect(player.getName(), block);
    }
    
    public void update(final CreativeBlockData data, final Block block) {
        this.update(data, block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
    
    public void update(final CreativeBlockData data, final String world, final int x, final int y, final int z) {
        this.queue("UPDATE `" + this.prefix + "blocks_" + world + "` SET `allowed` = '" + data.allowed + "', `owner` = '" + this.getPlayerId(data.owner) + "' WHERE x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "';");
    }
    
    public void protect(final String player, final Block block) {
        this.protect(player, block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getTypeId());
    }
    
    public void protect(final String player, final String world, final int x, final int y, final int z, final int type) {
        this.queue("INSERT INTO `" + this.prefix + "blocks_" + world + "` (owner, x, y, z, type, allowed, time) VALUES ('" + this.getPlayerId(player) + "', '" + x + "', '" + y + "', '" + z + "', '" + type + "', '" + (Object)null + "', '" + System.currentTimeMillis() + "');");
    }
    
    public void unprotect(final Block block) {
        this.unprotect(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
    
    public void unprotect(final String world, final int x, final int y, final int z) {
        this.queue("DELETE FROM `" + this.prefix + "blocks_" + world + "` WHERE x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "';");
    }
    
    public CreativeBlockData getFullData(final Location block) {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeBlockData data = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.getQuery("SELECT * FROM `" + this.prefix + "blocks_" + block.getWorld().getName() + "` WHERE x = '" + block.getX() + "' AND z = '" + block.getZ() + "' AND y = '" + block.getY() + "';", new Object[0]);
            rs = ps.getResultSet();
            if (rs.next()) {
                data = new CreativeBlockData(this.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "), Long.toString(rs.getLong("time")));
            }
            else if (CreativeSQLUpdater.lock) {
                ps = this.getQuery("SELECT * FROM `" + this.prefix + "blocks` WHERE location = " + LocationUtils.locationToString2(block) + "';", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    data = new CreativeBlockData(this.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "), rs.getString("time"));
                }
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get block from database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get block from database", new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(rs);
        }
        return data;
    }
    
    public CreativeBlockData isprotected(final Block block, final boolean nodrop) {
        return null;
    }
    
    public CreativeBlockData isprotected(final String world, final int x, final int y, final int z, final int type, final boolean nodrop) {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        CreativeBlockData data = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (nodrop) {
                ps = this.getQuery("SELECT owner, type, allowed FROM `" + this.prefix + "blocks_" + world + "` WHERE x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "';", new Object[0]);
            }
            else {
                ps = this.getQuery("SELECT type FROM `" + this.prefix + "blocks_" + world + "` WHERE x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "';", new Object[0]);
            }
            rs = ps.getResultSet();
            if (rs.next()) {
                if (nodrop) {
                    data = new CreativeBlockData(this.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                }
                else {
                    data = new CreativeBlockData(rs.getInt("type"));
                }
            }
            else if (CreativeSQLUpdater.lock) {
                ps = this.getQuery("SELECT owner, type, allowed FROM `" + this.prefix + "blocks` WHERE location = " + LocationUtils.locationToString2(world, x, y, z) + "';", new Object[0]);
                rs = ps.getResultSet();
                if (rs.next()) {
                    data = new CreativeBlockData(this.getPlayerName(rs.getInt("owner")), rs.getInt("type"), CollectionUtils.toStringHashSet(rs.getString("allowed"), ", "));
                }
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get block from database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get block from database", new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(rs);
        }
        if (data != null && data.type != 73 && data.type != 74 && data.type != type) {
            data = null;
        }
        return data;
    }
    
    public void load() {
        this.load(this.connection, this.getDatabaseEngine());
    }
    
    public void load(final Connection connection, final DBType type) {
        final Communicator com = CreativeControl.getPlugin().getCommunicator();
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "groups` (player INT, groups VARCHAR(255));", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "groups` table", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "players` ({auto}, player VARCHAR(255));", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "players` table", new Object[0]);
        }
        try {
            this.createIndex(connection, "CREATE INDEX `" + this.prefix + "names` ON `" + this.prefix + "players` (player);");
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "names` index", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "players_adventurer` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "players_adventurer` table", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "players_survival` ({auto}, player INT, health INT, foodlevel INT, exhaustion INT, saturation INT, experience INT, armor TEXT, inventory TEXT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "players_survival` table", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "players_creative` ({auto}, player INT, armor TEXT, inventory TEXT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "players_creative` table", new Object[0]);
        }
        for (final World world : Bukkit.getWorlds()) {
            this.load(connection, world.getName(), type);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "regions` ({auto}, name VARCHAR(255), start VARCHAR(255), end VARCHAR(255), type VARCHAR(255));", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "regions` table", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "friends` ({auto}, player INT, friends TEXT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "[TAG] Failed to create `" + this.prefix + "friends` table", new Object[0]);
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "internal` (version INT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "[TAG] Failed to create `" + this.prefix + "internal` table", new Object[0]);
        }
    }
    
    public void load(Connection connection, final String world, DBType type) {
        final Communicator com = CreativeControl.getPlugin().getCommunicator();
        if (connection == null) {
            connection = this.connection;
        }
        if (type == null) {
            type = this.getDatabaseEngine();
        }
        try {
            this.createTable(connection, "CREATE TABLE IF NOT EXISTS `" + this.prefix + "blocks_" + world + "` (owner INT, x INT, y INT, z INT, type INT, allowed VARCHAR(255), time BIGINT);", type);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "blocks_" + world + "` table", new Object[0]);
        }
        try {
            this.createIndex(connection, "CREATE INDEX `" + this.prefix + "block_" + world + "` ON `" + this.prefix + "blocks_" + world + "` (x, z, y);");
            this.createIndex(connection, "CREATE INDEX `" + this.prefix + "type_" + world + "` ON `" + this.prefix + "blocks_" + world + "` (type);");
            this.createIndex(connection, "CREATE INDEX `" + this.prefix + "owner_" + world + "` ON `" + this.prefix + "blocks_" + world + "` (owner);");
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to create `" + this.prefix + "blocks_" + world + "` index", new Object[0]);
        }
    }
    
    public String getPlayerName(final int id) {
        String ret = null;
        if (CreativeSQLDatabase.owners.containsValue(id)) {
            return CreativeSQLDatabase.owners.getKey(id);
        }
        final Communicator com = CreativeControl.plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.getQuery("SELECT player FROM `" + this.prefix + "players` WHERE id = '" + id + "' LIMIT 1;", new Object[0]);
            rs = ps.getResultSet();
            if (rs.next()) {
                ret = rs.getString("player");
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get the player data from the database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get the player data from the database", new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(rs);
        }
        CreativeSQLDatabase.owners.put(ret, id);
        return ret;
    }
    
    public int getPlayerId(final String player) {
        int ret = -1;
        if (CreativeSQLDatabase.owners.containsKey(player)) {
            return CreativeSQLDatabase.owners.get(player);
        }
        final Communicator com = CreativeControl.plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.getQuery("SELECT id FROM `" + this.prefix + "players` WHERE player = '" + player + "' LIMIT 1;", new Object[0]);
            rs = ps.getResultSet();
            if (rs.next()) {
                ret = rs.getInt("id");
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to retrieve " + player + "'s id", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to retrieve " + player + "'s id", new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(rs);
        }
        if (ret == -1) {
            try {
                this.execute("INSERT INTO `" + this.prefix + "players` (player) VALUES ('" + player + "');", new Object[0]);
            }
            catch (CoreException ex2) {
                com.error(ex2, "Failed to insert " + player + "'s id", new Object[0]);
                return -1;
            }
            return this.getPlayerId(player);
        }
        CreativeSQLDatabase.owners.put(player, ret);
        return ret;
    }
    
    public List<Integer> getAllPlayersId() {
        final List<Integer> ret = new ArrayList<Integer>();
        final Communicator com = CreativeControl.plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = this.getQuery("SELECT id FROM `" + this.prefix + "players`;", new Object[0]);
            rs = ps.getResultSet();
            while (rs.next()) {
                ret.add(rs.getInt("id"));
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get player data from the database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get all players id", new Object[0]);
        }
        finally {
            CoreSQLDatabase.closeQuietly(rs);
        }
        return ret;
    }
    
    static {
        CreativeSQLDatabase.owners = new CoreSafeCache<String, Integer>();
    }
}
