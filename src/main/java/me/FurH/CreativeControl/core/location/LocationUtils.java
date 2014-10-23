package me.FurH.CreativeControl.core.location;

import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.*;

public class LocationUtils
{
    @Deprecated
    public static String positionToString2(final Location loc) {
        return positionToString2(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    
    @Deprecated
    public static String positionToString2(final String world, final double x, final double y, final double z, final float yaw, final float pitch) {
        return world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }
    
    public static String positionToString(final Location loc) {
        return positionToString(loc.getX(), loc.getZ(), loc.getY(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName());
    }
    
    public static String positionToString(final double x, final double z, final double y, final float yaw, final float pitch, final String world) {
        return x + ":" + z + ":" + y + ":" + yaw + ":" + pitch + ":" + world;
    }
    
    @Deprecated
    public static String locationToString2(final Location loc) {
        return locationToString2(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    @Deprecated
    public static String locationToString2(final String world, final int x, final int y, final int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }
    
    public static String locationToString(final Location loc) {
        return locationToString(loc.getBlockX(), loc.getBlockZ(), loc.getBlockY(), loc.getWorld().getName());
    }
    
    public static String locationToString(final int x, final int z, final int y, final String world) {
        return x + ":" + z + ":" + y + ":" + world;
    }
    
    @Deprecated
    public static Location stringToPosition2(final String string) throws CoreException {
        try {
            final String[] split = string.split(":");
            final World w = Bukkit.getWorld(split[0]);
            if (w != null) {
                return new Location(w, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the position string: " + string);
        }
        return null;
    }
    
    public static Location stringToPosition(final String string) throws CoreException {
        try {
            final String[] split = string.split(":");
            final World w = Bukkit.getWorld(split[5]);
            if (w != null) {
                return new Location(w, Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[1]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the position string: " + string);
        }
        return null;
    }
    
    public static Location stringToLocation(final String string) throws CoreException {
        try {
            final String[] split = string.split(":");
            final World w = Bukkit.getWorld(split[3]);
            if (w != null) {
                return new Location(w, Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[1]));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the location string: " + string);
        }
        return null;
    }
    
    @Deprecated
    public static Location stringToLocation2(final String string) throws CoreException {
        try {
            final String[] split = string.split(":");
            final World w = Bukkit.getWorld(split[0]);
            if (w != null) {
                return new Location(w, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
            }
        }
        catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the location string: " + string);
        }
        return null;
    }
}
