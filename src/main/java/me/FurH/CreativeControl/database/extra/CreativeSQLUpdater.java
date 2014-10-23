package me.FurH.CreativeControl.database.extra;

import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.database.*;
import java.util.*;
import java.sql.*;

public class CreativeSQLUpdater implements Runnable
{
    private HashSet<String> convert;
    private HashSet<String> tables;
    private CreativeControl plugin;
    public static boolean lock;
    private Player p;
    
    public CreativeSQLUpdater(final CreativeControl plugin) {
        super();
        this.convert = new HashSet<String>();
        this.tables = new HashSet<String>();
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (CreativeSQLUpdater.lock) {
            System.out.println("Updater Locked");
            return;
        }
        CreativeSQLUpdater.lock = true;
        final long start = System.currentTimeMillis();
        final Communicator com = this.plugin.getCommunicator();
        com.msg((CommandSender)this.p, "&7Initializing... ", new Object[0]);
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final List<String> thistables = new ArrayList<String>();
        thistables.add(db.prefix + "players_survival");
        thistables.add(db.prefix + "players_creative");
        thistables.add(db.prefix + "players_adventurer");
        thistables.add(db.prefix + "friends");
        try {
            for (final String table : thistables) {
                try {
                    final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT 1;", new Object[0]);
                    final ResultSet rs = ps.getResultSet();
                    if (!rs.next()) {
                        continue;
                    }
                    final String player = rs.getString("player");
                }
                catch (Exception ex2) {
                    db.execute("ALTER TABLE `" + table + "` RENAME TO `old_" + table + "`;", new Object[0]);
                    this.convert.add(table);
                }
            }
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to rename older tables", new Object[0]);
        }
        try {
            db.commit();
        }
        catch (CoreException ex) {
            com.error(ex);
        }
        db.load();
        try {
            db.commit();
        }
        catch (CoreException ex) {
            com.error(ex);
        }
        this.update_players_creative_2();
        this.update_players_survival_2();
        this.update_players_adventurer_2();
        this.update_friends_2();
        this.update_blocks_2();
        try {
            db.incrementVersion(2);
        }
        catch (CoreException ex) {
            com.error(ex, "Failed to increment database version", new Object[0]);
        }
        com.msg((CommandSender)this.p, "&7All data updated in &4{0}&7 ms", System.currentTimeMillis() - start);
        CreativeSQLUpdater.lock = false;
    }
    
    public void update_blocks_2() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long blocks_start = System.currentTimeMillis();
        final String table = db.prefix + "blocks";
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
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
                    final String[] location = rs.getString("location").split(":");
                    final String table2 = db.prefix + "blocks_" + location[0];
                    if (!this.tables.contains(table2)) {
                        if (!db.hasTable(table2)) {
                            db.load(db.connection, location[0], db.getDatabaseEngine());
                            db.commit();
                        }
                        this.tables.add(table2);
                    }
                    final PreparedStatement ps2 = db.prepare("INSERT INTO `" + db.prefix + "blocks_" + location[0] + "` (owner, x, y, z, type, allowed, time) VALUES (?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, db.getPlayerId(rs.getString("owner")));
                    ps2.setInt(2, Integer.parseInt(location[1]));
                    ps2.setInt(3, Integer.parseInt(location[2]));
                    ps2.setInt(4, Integer.parseInt(location[3]));
                    ps2.setInt(5, rs.getInt("type"));
                    ps2.setString(6, rs.getString("allowed"));
                    ps2.setLong(7, rs.getLong("time"));
                    ps2.execute();
                    ++blocks_done;
                    ++row;
                }
                db.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while running the update method 'update_blocks_2'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_blocks_2'", new Object[0]);
                break;
            }
        }
        final long blocks_time = System.currentTimeMillis() - blocks_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", blocks_time);
    }
    
    public void update_players_creative_2() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long creative_start = System.currentTimeMillis();
        final String table = db.prefix + "players_creative";
        if (!this.convert.contains(table)) {
            return;
        }
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
        double creative_size = 0.0;
        try {
            creative_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + creative_size, new Object[0]);
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
                final PreparedStatement ps = db.getQuery("SELECT * FROM `old_" + table + "` LIMIT " + (int)creative_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = db.prepare("INSERT INTO `" + table + "` (player, armor, inventory) VALUES (?, ?, ?);");
                    ps2.setInt(1, db.getPlayerId(rs.getString("player")));
                    ps2.setString(2, rs.getString("armor"));
                    ps2.setString(3, rs.getString("inventory"));
                    ps2.execute();
                    ++creative_done;
                    ++row;
                }
                db.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while running the update method 'update_players_creative_2'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_players_creative_2'", new Object[0]);
                break;
            }
        }
        final long creative_time = System.currentTimeMillis() - creative_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", creative_time);
    }
    
    public void update_players_survival_2() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long survival_start = System.currentTimeMillis();
        final String table = db.prefix + "players_survival";
        if (!this.convert.contains(table)) {
            return;
        }
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
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
                final PreparedStatement ps = db.getQuery("SELECT * FROM `old_" + table + "` LIMIT " + (int)survival_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = db.prepare("INSERT INTO `" + table + "` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, db.getPlayerId(rs.getString("player")));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    ps2.execute();
                    ++survival_done;
                    ++row;
                }
                db.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while running the update method 'update_players_survival_2'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_players_survival_2'", new Object[0]);
                break;
            }
        }
        final long survival_time = System.currentTimeMillis() - survival_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", survival_time);
    }
    
    public void update_players_adventurer_2() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long adventurer_start = System.currentTimeMillis();
        final String table = db.prefix + "players_adventurer";
        if (!this.convert.contains(table)) {
            return;
        }
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
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
                final PreparedStatement ps = db.getQuery("SELECT * FROM `old_" + table + "` LIMIT " + (int)adventurer_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = db.prepare("INSERT INTO `" + table + "` (player, health, foodlevel, exhaustion, saturation, experience, armor, inventory) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, db.getPlayerId(rs.getString("player")));
                    ps2.setInt(2, rs.getInt("health"));
                    ps2.setInt(3, rs.getInt("foodlevel"));
                    ps2.setInt(4, rs.getInt("exhaustion"));
                    ps2.setInt(5, rs.getInt("saturation"));
                    ps2.setInt(6, rs.getInt("experience"));
                    ps2.setString(7, rs.getString("armor"));
                    ps2.setString(8, rs.getString("inventory"));
                    ps2.execute();
                    ++adventurer_done;
                    ++row;
                }
                db.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while running the update method 'update_players_adventurer_2'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_players_adventurer_2'", new Object[0]);
                break;
            }
        }
        final long adventurer_time = System.currentTimeMillis() - adventurer_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", adventurer_time);
    }
    
    public void update_friends_2() {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long friends_start = System.currentTimeMillis();
        final String table = db.prefix + "friends";
        if (!this.convert.contains(table)) {
            return;
        }
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
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
                final PreparedStatement ps = db.getQuery("SELECT * FROM `old_" + table + "` LIMIT " + (int)friends_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final PreparedStatement ps2 = db.prepare("INSERT INTO `" + table + "` (player, friends) VALUES (?, ?);");
                    ps2.setInt(1, db.getPlayerId(rs.getString("player")));
                    ps2.setString(2, rs.getString("friends"));
                    ps2.execute();
                    ++friends_done;
                    ++row;
                }
                db.commit();
                rs.close();
                ps.close();
                if (row < 10000) {
                    break;
                }
                continue;
            }
            catch (CoreException ex) {
                com.error(ex, "An error occurried while running the update method 'update_friends_2'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_friends_2'", new Object[0]);
                break;
            }
        }
        final long friends_time = System.currentTimeMillis() - friends_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", friends_time);
    }
    
    static {
        CreativeSQLUpdater.lock = false;
    }
}
