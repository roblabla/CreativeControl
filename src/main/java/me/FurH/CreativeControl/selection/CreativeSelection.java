package me.FurH.CreativeControl.selection;

import org.bukkit.*;
import org.bukkit.util.*;

public class CreativeSelection
{
    private Location start;
    private Location end;
    private Vector vector;
    
    public CreativeSelection(final Location start, final Location end) {
        super();
        int sx = start.getBlockX();
        int ex = end.getBlockX();
        if (sx > ex) {
            final int i = sx;
            sx = ex;
            ex = i;
        }
        int sy = start.getBlockY();
        int ey = end.getBlockY();
        if (sy > ey) {
            final int j = sy;
            sy = ey;
            ey = j;
        }
        int sz = start.getBlockZ();
        int ez = end.getBlockZ();
        if (sz > ez) {
            final int k = sz;
            sz = ez;
            ez = k;
        }
        this.start = new Location(start.getWorld(), (double)sx, (double)sy, (double)sz);
        this.end = new Location(start.getWorld(), (double)ex, (double)ey, (double)ez);
    }
    
    public Vector getVector() {
        return this.vector = new Vector(Math.abs(this.end.getBlockX() - this.start.getBlockX()), Math.abs(this.end.getBlockY() - this.start.getBlockY()), Math.abs(this.end.getBlockZ() - this.start.getBlockZ()));
    }
    
    public void expandUpUp(final int up) {
        this.end.add(0.0, (double)up, 0.0);
    }
    
    public void contratUpUp(final int contrat) {
        this.end.subtract(0.0, (double)contrat, 0.0);
    }
    
    public void expandUpDown(final int up) {
        this.start.add(0.0, (double)up, 0.0);
    }
    
    public void contratUpDown(final int contrat) {
        this.start.subtract(0.0, (double)contrat, 0.0);
    }
    
    public void expandVert() {
        this.start.setY(0.0);
        this.end.setY(255.0);
    }
    
    public Location getStart() {
        return this.start;
    }
    
    public Location getEnd() {
        return this.end;
    }
    
    public int getArea() {
        final Location min = this.start;
        final Location max = this.end;
        return (max.getBlockX() - min.getBlockX() + 1) * (max.getBlockY() - min.getBlockY() + 1) * (max.getBlockZ() - min.getBlockZ() + 1);
    }
}
