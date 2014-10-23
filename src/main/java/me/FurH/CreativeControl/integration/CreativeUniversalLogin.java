package me.FurH.CreativeControl.integration;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class CreativeUniversalLogin extends Event
{
    private static final HandlerList handlers;
    private Player player;
    
    public CreativeUniversalLogin(final Player player) {
        super();
        this.player = player;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public HandlerList getHandlers() {
        return CreativeUniversalLogin.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return CreativeUniversalLogin.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
