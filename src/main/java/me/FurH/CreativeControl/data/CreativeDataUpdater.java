package me.FurH.CreativeControl.data;

import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.database.*;
import java.util.*;
import java.sql.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.core.inventory.*;

public class CreativeDataUpdater
{
    public static boolean lock;
    private CreativeControl plugin;
    private Player p;
    
    public CreativeDataUpdater(final CreativeControl plugin) {
        super();
        this.plugin = plugin;
    }
    
    public void run() {
        if (CreativeDataUpdater.lock) {
            System.out.println("Updater Locked");
            return;
        }
        CreativeDataUpdater.lock = true;
        final long start = System.currentTimeMillis();
        final Communicator com = this.plugin.getCommunicator();
        com.msg((CommandSender)this.p, "&7Initializing... ", new Object[0]);
        final CreativeSQLDatabase db = CreativeControl.getDb();
        db.load();
        try {
            db.commit();
        }
        catch (CoreException ex) {
            com.error(ex);
        }
        final List<String> tables = new ArrayList<String>();
        tables.add(db.prefix + "players_adventurer");
        tables.add(db.prefix + "players_survival");
        tables.add(db.prefix + "players_creative");
        for (final String table : tables) {
            this.update_players_table_3(table);
        }
        try {
            db.incrementVersion(3);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to increment the database version", new Object[0]);
        }
        com.msg((CommandSender)this.p, "&7All data updated in &4{0}&7 ms", System.currentTimeMillis() - start);
        CreativeDataUpdater.lock = false;
    }
    
    public void update_players_table_3(final String table) {
        final Communicator com = this.plugin.getCommunicator();
        final CreativePlayerData data = CreativeControl.getPlayerData();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final long adventurer_start = System.currentTimeMillis();
        com.msg((CommandSender)this.p, "&7Updating table '&4" + table + "&7' ...", new Object[0]);
        double table_size = 0.0;
        try {
            table_size = db.getTableCount(table);
        }
        catch (CoreException ex3) {}
        com.msg((CommandSender)this.p, "&7Table size: &4" + table_size, new Object[0]);
        double table_processed = 0.0;
        double table_done = 0.0;
        double table_last = 0.0;
        while (true) {
            table_processed = table_done / table_size * 100.0;
            int row = 0;
            if (table_processed - table_last >= 5.0) {
                System.gc();
                com.msg((CommandSender)this.p, "&4{0}&7 of ~&4{1}&7 queries processed, &4{2}&7%", table_done, table_size, String.format("%d", (int)table_processed));
                table_last = table_processed;
            }
            try {
                final PreparedStatement ps = db.getQuery("SELECT * FROM `" + table + "` LIMIT " + (int)table_done + ", " + 10000 + ";", new Object[0]);
                final ResultSet rs = ps.getResultSet();
                while (rs.next()) {
                    final int id = rs.getInt("player");
                    final ItemStack[] armor = this.toArrayStack(rs.getString("armor"));
                    final ItemStack[] contents = this.toArrayStack(rs.getString("inventory"));
                    db.execute("UPDATE `" + table + "` SET armor = '" + InventoryStack.getStringFromArray(armor) + "', inventory = '" + InventoryStack.getStringFromArray(contents) + "' WHERE player = '" + id + "';", new Object[0]);
                    ++table_done;
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
                com.error(ex, "An error occurried while running the update method 'update_players_table_3'", new Object[0]);
                break;
            }
            catch (SQLException ex2) {
                com.error(ex2, "An error occurried while running the update method 'update_players_table_3'", new Object[0]);
                break;
            }
        }
        final long table_time = System.currentTimeMillis() - adventurer_start;
        com.msg((CommandSender)this.p, "&7Table '&4" + table + "&7' updated in &4{0}&7 ms", table_time);
    }
    
    public ItemStack[] toArrayStack(final String string) {
        return InvUtils.toArrayStack(string);
    }
    
    static {
        CreativeDataUpdater.lock = false;
    }
}
