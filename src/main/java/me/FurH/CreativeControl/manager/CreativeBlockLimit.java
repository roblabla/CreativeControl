package me.FurH.CreativeControl.manager;

public class CreativeBlockLimit
{
    private long expire;
    private int placed;
    
    public CreativeBlockLimit() {
        super();
        this.placed = 0;
        this.expire = System.currentTimeMillis() + 60000L;
    }
    
    public boolean isExpired() {
        return this.expire < System.currentTimeMillis();
    }
    
    public void increment() {
        ++this.placed;
    }
    
    public int getPlaced() {
        return this.placed;
    }
    
    public void reset() {
        this.expire = System.currentTimeMillis() + 60000L;
        this.placed = 0;
    }
}
