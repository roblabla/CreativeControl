package me.FurH.CreativeControl.database.extra;

import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;
import me.FurH.CreativeControl.core.location.*;
import me.FurH.CreativeControl.database.*;
import java.sql.*;

public class CreativeSQLCleanup implements Runnable
{
    private CreativeControl plugin;
    public static boolean lock;
    private Player p;
    
    public CreativeSQLCleanup(final CreativeControl plugin, final Player player) {
        super();
        this.plugin = plugin;
        this.p = player;
    }
    
    @Override
    public void run() {
        if (CreativeSQLCleanup.lock) {
            System.out.println("Cleanup Locked");
            return;
        }
        CreativeSQLCleanup.lock = true;
        final long start = System.currentTimeMillis();
        final Communicator com = this.plugin.getCommunicator();
        com.msg((CommandSender)this.p, "&7Initializing... ", new Object[0]);
        for (final World world : Bukkit.getWorlds()) {
            this.cleanup_blocks(world);
        }
        com.msg((CommandSender)this.p, "&7All tables cleaned in &4{0}&7 ms", System.currentTimeMillis() - start);
        CreativeSQLCleanup.lock = false;
    }
    
    public void cleanup_blocks(final World w) {
        final Communicator com = this.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long blocks_start = System.currentTimeMillis();
        final String table = db.prefix + "blocks_" + w.getName();
        com.msg((CommandSender)this.p, "&7Cleaning table '&4" + table + "&7' ...", new Object[0]);
        double blocks_size = 0.0;
        try {
            blocks_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "Table size: &4" + blocks_size, new Object[0]);
        double blocks_process = 0.0;
        double blocks_done = 0.0;
        double blocks_last = 0.0;
        double blocks_removed = 0.0;
        final HashSet<String> locations = new HashSet<String>();
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
                    boolean delete = false;
                    final int x = rs.getInt("x");
                    final int y = rs.getInt("y");
                    final int z = rs.getInt("z");
                    final int type = rs.getInt("type");
                    final int id = w.getBlockTypeIdAt(x, y, z);
                    if (type != id) {
                        com.msg((CommandSender)this.p, "&7Invalid block at X: &4" + x + "&7, Y: &4" + y + "&7, Z: &4" + z + "&7, I1: &4" + type + "&7, I2: &4" + id, new Object[0]);
                        delete = true;
                    }
                    final String loc = LocationUtils.locationToString(x, y, z, w.getName());
                    if (!locations.contains(loc)) {
                        locations.add(loc);
                    }
                    else {
                        com.msg((CommandSender)this.p, "&7Duplicated block at X: &4" + x + "&7, Y: &4" + y + "&7, Z: &4" + z, new Object[0]);
                        delete = true;
                    }
                    if (delete) {
                        db.execute("DELETE FROM `" + table + "` WHERE x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND time = '" + rs.getString("time") + "';", new Object[0]);
                        ++blocks_removed;
                    }
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
                com.error(ex, "An error ocurried while cleaning the database", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error ocurried while cleaning the database", new Object[0]);
                break;
            }
        }
        final long survival_time = System.currentTimeMillis() - blocks_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' cleaned in &4{0}&7 ms", survival_time);
    }
    
    static {
        CreativeSQLCleanup.lock = false;
    }
}
