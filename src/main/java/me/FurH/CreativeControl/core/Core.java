package me.FurH.CreativeControl.core;

public class Core extends CorePlugin
{
    public static long start;
    
    public Core() {
        super("&8[&3CoreLib&8]&7:&f");
    }
    
    public void onEnable() {
        this.logEnable();
    }
    
    public void onDisable() {
        this.logDisable();
    }
    
    static {
        Core.start = 0L;
    }
}
