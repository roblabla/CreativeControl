package me.FurH.CreativeControl.data;

import org.bukkit.inventory.*;
import org.bukkit.*;

public class CreativePlayerCache
{
    public ItemStack[] armor;
    public double health;
    public int food;
    public float ex;
    public int id;
    public int exp;
    public float sat;
    public String name;
    public ItemStack[] items;
    
    public CreativePlayerCache() {
        super();
        this.armor = new ItemStack[] { new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR) };
        this.health = 20.0;
        this.food = 20;
        this.ex = 0.0f;
        this.id = -1;
        this.exp = 0;
        this.sat = 0.0f;
        this.name = "";
        this.items = new ItemStack[] { new ItemStack(Material.AIR) };
    }
}
