package me.FurH.CreativeControl.core.packets;

import me.FurH.CreativeControl.core.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.packets.objects.*;

public abstract class IPacketQueue
{
    private String owner;
    
    public IPacketQueue(final CorePlugin plugin) {
        super();
        this.owner = plugin.getDescription().getName();
    }
    
    public boolean handleAsyncCustomPayload(final Player player, final PacketCustomPayload packet) {
        return true;
    }
    
    public PacketCustomPayload handleAndSetAsyncCustomPayload(final Player player, final PacketCustomPayload object) {
        return object;
    }
    
    public boolean handleAsyncClientSettings(final Player player) {
        return true;
    }
    
    public PacketMapChunkBulk handleAsyncMapChunkBulk(final Player player, final PacketMapChunkBulk object) {
        return object;
    }
    
    public PacketMapChunk handleAsyncMapChunk(final Player player, final PacketMapChunk object) {
        return object;
    }
    
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + ((this.owner != null) ? this.owner.hashCode() : 0);
        return hash;
    }
    
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final IPacketQueue other = (IPacketQueue)obj;
        if (this.owner == null) {
            if (other.owner == null) {
                return true;
            }
        }
        else if (this.owner.equals(other.owner)) {
            return true;
        }
        return false;
    }
}
