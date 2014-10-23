package me.FurH.CreativeControl.region;

import org.bukkit.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.core.location.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.database.*;
import java.sql.*;
import java.util.*;

public class CreativeRegionManager
{
    private List<CreativeRegion> areas;
    
    public CreativeRegionManager() {
        super();
        this.areas = new ArrayList<CreativeRegion>();
    }
    
    public List<CreativeRegion> getAreas() {
        return this.areas;
    }
    
    public CreativeRegion getRegion(final Location loc) {
        if (loc == null) {
            return null;
        }
        for (final CreativeRegion region : this.areas) {
            if (region == null) {
                continue;
            }
            if (region.contains(loc)) {
                return region;
            }
        }
        return null;
    }
    
    public void addRegion(final String name, final Location start, final Location end, final String type) {
        final CreativeRegion region = new CreativeRegion();
        region.start = start;
        region.end = end;
        if (type.equalsIgnoreCase("CREATIVE")) {
            region.gamemode = GameMode.CREATIVE;
        }
        else if (type.equalsIgnoreCase("ADVENTURE")) {
            region.gamemode = GameMode.ADVENTURE;
        }
        else {
            region.gamemode = GameMode.SURVIVAL;
        }
        region.name = name;
        this.areas.add(region);
    }
    
    public int loadRegions() {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        int total = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db.getQuery("SELECT * FROM `" + db.prefix + "regions`", new Object[0]);
            rs = ps.getResultSet();
            while (rs.next()) {
                final String name = rs.getString("name");
                final Location start = LocationUtils.stringToLocation2(rs.getString("start"));
                final Location end = LocationUtils.stringToLocation2(rs.getString("end"));
                final String type = rs.getString("type");
                this.addRegion(name, start, end, type);
                ++total;
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get regions from the database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get regions from the database", new Object[0]);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException ex3) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (SQLException ex4) {}
            }
        }
        return total;
    }
    
    public boolean getRegion(final String name) {
        final Communicator com = CreativeControl.plugin.getCommunicator();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = db.getQuery("SELECT * FROM `" + db.prefix + "regions` WHERE name = '" + name + "'", new Object[0]);
            rs = ps.getResultSet();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException ex) {
            com.error(ex, "Failed to get region from the database", new Object[0]);
        }
        catch (CoreException ex2) {
            com.error(ex2, "Failed to get region from the database", new Object[0]);
        }
        finally {
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException ex3) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (SQLException ex4) {}
            }
        }
        return false;
    }
    
    public void deleteRegion(final String name) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        this.deleteRegionCache(name);
        db.queue("DELETE FROM `" + db.prefix + "regions` WHERE name = '" + name + "'");
    }
    
    public void deleteRegionCache(final String name) {
        final List<CreativeRegion> remove = new ArrayList<CreativeRegion>();
        for (final CreativeRegion region : this.areas) {
            if (region.name.equalsIgnoreCase(name)) {
                remove.add(region);
            }
        }
        this.areas.removeAll(remove);
        remove.clear();
    }
    
    public void saveRegion(final String name, final GameMode type, final Location start, final Location end) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        this.deleteRegionCache(name);
        if (!this.getRegion(name)) {
            this.addRegion(name, start, end, type.toString());
            db.queue("INSERT INTO `" + db.prefix + "regions` (name, start, end, type) VALUES ('" + name + "', '" + LocationUtils.locationToString2(start) + "', '" + LocationUtils.locationToString2(end) + "', '" + type.toString() + "')");
        }
        else {
            this.addRegion(name, start, end, type.toString());
            db.queue("UPDATE `" + db.prefix + "regions` SET start = '" + LocationUtils.locationToString2(start) + "', end = '" + LocationUtils.locationToString2(end) + "', type = '" + type.toString() + "' WHERE name = '" + name + "'");
        }
    }
}
