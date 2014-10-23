package me.FurH.CreativeControl.database.extra;

import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.database.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.database.*;
import java.util.*;
import java.sql.*;

public class CreativeSQLMigrator implements Runnable
{
    private CreativeControl plugin;
    public static boolean lock;
    private String data;
    private Player p;
    private CoreSQLDatabase.DBType type;
    private Connection to;
    
    public CreativeSQLMigrator(final CreativeControl plugin, final Player p, final String data) {
        super();
        this.plugin = plugin;
        this.data = data;
        this.p = p;
    }
    
    @Override
    public void run() {
        if (CreativeSQLMigrator.lock) {
            System.out.println("Migrator Locked");
            return;
        }
        CreativeSQLMigrator.lock = true;
        final long start = System.currentTimeMillis();
        final Communicator com = this.plugin.getCommunicator();
        com.msg((CommandSender)this.p, "&7Initializing... ", new Object[0]);
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (this.data.equalsIgnoreCase(">SQLite")) {
            com.msg((CommandSender)this.p, "&7Connecting to the &4SQLite&7 database...", new Object[0]);
            try {
                this.to = db.getSQLiteConnection();
            }
            catch (CoreException ex) {
                com.error(ex, "Failed to connect to the destination database", new Object[0]);
            }
            final CoreSQLDatabase.DBType type = this.type;
            this.type = CoreSQLDatabase.DBType.SQLite;
        }
        if (this.data.equalsIgnoreCase(">MySQL")) {
            com.msg((CommandSender)this.p, "&7Connecting to &4MySQL database...", new Object[0]);
            try {
                this.to = db.getMySQLConnection();
            }
            catch (CoreException ex) {
                com.error(ex, "Failed to connect to the destination database", new Object[0]);
            }
            final CoreSQLDatabase.DBType type2 = this.type;
            this.type = CoreSQLDatabase.DBType.MySQL;
        }
        com.msg((CommandSender)this.p, "&7Initializing database...", new Object[0]);
        try {
            this.to.setAutoCommit(false);
            this.to.commit();
        }
        catch (SQLException ex2) {
            com.error(ex2, "Failed to set autocommit state", new Object[0]);
        }
        db.load(this.to, this.type);
        try {
            this.to.commit();
        }
        catch (SQLException ex2) {
            com.error(ex2);
        }
        this.move_regions();
        this.move_players_survival();
        this.move_players_creative();
        this.move_players_adventurer();
        this.move_players();
        this.move_internal();
        this.move_friends();
        final List<String> tables = new ArrayList<String>();
        for (final World world : Bukkit.getWorlds()) {
            tables.add(db.prefix + "blocks_" + world.getName());
        }
        for (final String table : tables) {
            this.move_blocks(table);
        }
        com.msg((CommandSender)this.p, "&7All data moved in &4{0}&7 ms", System.currentTimeMillis() - start);
        CreativeSQLMigrator.lock = false;
    }
    
    public void move_blocks(final String table) {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long blocks_start = System.currentTimeMillis();
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double blocks_size = 0.0;
        try {
            blocks_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + blocks_size, new Object[0]);
        double blocks_process = 0.0;
        double blocks_done = 0.0;
        double blocks_last = 0.0;
        while (true) {
            blocks_process = blocks_done / blocks_size * 100.0;
            int row = 0;
            if (blocks_process - blocks_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", blocks_done, blocks_size, String.format("%d", (int)blocks_process));
                blocks_last = blocks_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)blocks_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (owner, x, y, z, type, allowed, time) VALUES (?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, rs.getInt("owner"));
                    ps2.setInt(2, rs.getInt("x"));
                    ps2.setInt(3, rs.getInt("y"));
                    ps2.setInt(4, rs.getInt("z"));
                    ps2.setInt(5, rs.getInt("type"));
                    ps2.setString(6, rs.getString("allowed"));
                    ps2.setLong(7, rs.getLong("time"));
                    ps2.execute();
                    ps2.close();
                    ++blocks_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long blocks_time = System.currentTimeMillis() - blocks_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", blocks_time);
    }
    
    public void move_regions() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long regions_start = System.currentTimeMillis();
        final String table = db.prefix + "regions";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double regions_size = 0.0;
        try {
            regions_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + regions_size, new Object[0]);
        double regions_process = 0.0;
        double regions_done = 0.0;
        double regions_last = 0.0;
        while (true) {
            regions_process = regions_done / regions_size * 100.0;
            int row = 0;
            if (regions_process - regions_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", regions_done, regions_size, String.format("%d", (int)regions_process));
                regions_last = regions_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)regions_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (name, start, end, type) VALUES (?, ?, ?, ?);");
                    ps2.setString(1, rs.getString("name"));
                    ps2.setString(2, rs.getString("start"));
                    ps2.setString(3, rs.getString("end"));
                    ps2.setString(4, rs.getString("type"));
                    ps2.execute();
                    ps2.close();
                    ++regions_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long regions_time = System.currentTimeMillis() - regions_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", regions_time);
    }
    
    public void move_players_survival() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long survival_start = System.currentTimeMillis();
        final String table = db.prefix + "players_survival";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double survival_size = 0.0;
        try {
            survival_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + survival_size, new Object[0]);
        double survival_process = 0.0;
        double survival_done = 0.0;
        double survival_last = 0.0;
        while (true) {
            survival_process = survival_done / survival_size * 100.0;
            int row = 0;
            if (survival_process - survival_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", survival_done, survival_size, String.format("%d", (int)survival_process));
                survival_last = survival_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)survival_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    ps2.execute();
                    ps2.close();
                    ++survival_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long survival_time = System.currentTimeMillis() - survival_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", survival_time);
    }
    
    public void move_players_creative() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long creative_start = System.currentTimeMillis();
        final String table = db.prefix + "players_creative";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double creative_size = 0.0;
        try {
            creative_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size:&4 " + creative_size, new Object[0]);
        double creative_process = 0.0;
        double creative_done = 0.0;
        double creative_last = 0.0;
        while (true) {
            creative_process = creative_done / creative_size * 100.0;
            int row = 0;
            if (creative_process - creative_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", creative_done, creative_size, String.format("%d", (int)creative_process));
                creative_last = creative_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)creative_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (player, armor, inventory) VALUES (?, ?, ?);");
                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setString(2, rs.getString("armor"));
                    ps2.setString(3, rs.getString("inventory"));
                    ps2.execute();
                    ps2.close();
                    ++creative_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long creative_time = System.currentTimeMillis() - creative_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", creative_time);
    }
    
    public void move_players_adventurer() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long adventurer_start = System.currentTimeMillis();
        final String table = db.prefix + "players_adventurer";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double adventurer_size = 0.0;
        try {
            adventurer_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + adventurer_size, new Object[0]);
        double adventurer_process = 0.0;
        double adventurer_done = 0.0;
        double adventurer_last = 0.0;
        while (true) {
            adventurer_process = adventurer_done / adventurer_size * 100.0;
            int row = 0;
            if (adventurer_process - adventurer_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", adventurer_done, adventurer_size, String.format("%d", (int)adventurer_process));
                adventurer_last = adventurer_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)adventurer_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    ps2.execute();
                    ps2.close();
                    ++adventurer_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long adventurer_time = System.currentTimeMillis() - adventurer_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", adventurer_time);
    }
    
    public void move_players() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long players_start = System.currentTimeMillis();
        final String table = db.prefix + "players";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double players_size = 0.0;
        try {
            players_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + players_size, new Object[0]);
        double players_process = 0.0;
        double players_done = 0.0;
        double player_last = 0.0;
        while (true) {
            players_process = players_done / players_size * 100.0;
            int row = 0;
            if (players_process - player_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "{0} of ~{1} queries processed, {2}%", players_done, players_size, String.format("%d", (int)players_process));
                player_last = players_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)players_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (id, player) VALUES (?, ?);");
                    ps2.setInt(1, rs.getInt("id"));
                    ps2.setString(2, rs.getString("player"));
                    ps2.execute();
                    ps2.close();
                    ++players_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long players_time = System.currentTimeMillis() - players_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", players_time);
    }
    
    public void move_internal() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long internal_start = System.currentTimeMillis();
        final String table = db.prefix + "internal";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double internal_size = 0.0;
        try {
            internal_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + internal_size, new Object[0]);
        double internal_process = 0.0;
        double internal_done = 0.0;
        double internal_last = 0.0;
        while (true) {
            internal_process = internal_done / internal_size * 100.0;
            int row = 0;
            if (internal_process - internal_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", internal_done, internal_size, String.format("%d", (int)internal_process));
                internal_last = internal_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)internal_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (version) VALUES (?);");
                    ps2.setInt(1, rs.getInt("version"));
                    ps2.execute();
                    ps2.close();
                    ++internal_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long internal_time = System.currentTimeMillis() - internal_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", internal_time);
    }
    
    public void move_friends() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long friends_start = System.currentTimeMillis();
        final String table = db.prefix + "friends";
        com.msg((CommandSender)this.p, "&7Moving table '&4" + table + "&7' ...", new Object[0]);
        double friends_size = 0.0;
        try {
            friends_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + friends_size, new Object[0]);
        double friends_process = 0.0;
        double friends_done = 0.0;
        double friends_last = 0.0;
        while (true) {
            friends_process = friends_done / friends_size * 100.0;
            int row = 0;
            if (friends_process - friends_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", friends_done, friends_size, String.format("%d", (int)friends_process));
                friends_last = friends_process;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)friends_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = this.to.prepareStatement("INSERT INTO `" + table + "` (player, friends) VALUES (?, ?);");
                    ps2.setInt(1, rs.getInt("player"));
                    ps2.setString(2, rs.getString("friends"));
                    ps2.execute();
                    ps2.close();
                    ++friends_done;
                    ++row;
                }
                this.to.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while migrating the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while migrating the database", new Object[0]);
                break;
            }
        }
        final long friends_time = System.currentTimeMillis() - friends_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' moved in &4{0}&7 ms", friends_time);
    }
    
    static {
        CreativeSQLMigrator.lock = false;
    }
}
