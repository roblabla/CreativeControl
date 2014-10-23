package me.FurH.CreativeControl.region;

import org.bukkit.*;

public class CreativeRegion
{
    public Location start;
    public Location end;
    public GameMode gamemode;
    public String name;
    
    public boolean contains(final Location loc) {
        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();
        return x >= this.start.getBlockX() && x <= this.end.getBlockX() && y >= this.start.getBlockY() && y <= this.end.getBlockY() && z >= this.start.getBlockZ() && z <= this.end.getBlockZ();
    }
}
