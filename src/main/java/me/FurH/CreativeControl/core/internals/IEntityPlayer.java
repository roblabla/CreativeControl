package me.FurH.CreativeControl.core.internals;

import org.bukkit.entity.*;
import java.util.*;
import me.FurH.CreativeControl.core.inventory.*;
import java.lang.reflect.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.packets.*;
import me.FurH.CreativeControl.core.packets.objects.*;

public abstract class IEntityPlayer
{
    private static Class<?> packetCLS;
    public List send_later;
    public List send_replace;
    protected boolean inventory_hidden;
    protected Object entity;
    protected Player player;
    protected Object playerConnection;
    protected Object networkManager;
    
    public IEntityPlayer() {
        super();
        this.send_later = new ArrayList();
        this.send_replace = new ArrayList();
        this.inventory_hidden = false;
    }
    
    public IEntityPlayer setEntityPlayer(final Player player) {
        this.player = player;
        Class<?> craftPlayer = null;
        try {
            craftPlayer = Class.forName("org.bukkit.craftbukkit." + InternalManager.getServerVersion() + "entity.CraftPlayer");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        try {
            if (IEntityPlayer.packetCLS == null) {
                IEntityPlayer.packetCLS = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "Packet");
            }
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        final Object converted = InventoryStack.convert(player, craftPlayer);
        Method handle = null;
        try {
            handle = converted.getClass().getMethod("getHandle", (Class<?>[])new Class[0]);
        }
        catch (Exception ex2) {
            ex2.printStackTrace();
        }
        try {
            this.entity = handle.invoke(converted, new Object[0]);
        }
        catch (Exception ex2) {
            ex2.printStackTrace();
        }
        try {
            this.playerConnection = this.entity.getClass().getField("playerConnection").get(this.entity);
            this.networkManager = this.playerConnection.getClass().getField("networkManager").get(this.playerConnection);
        }
        catch (Exception ex2) {
            ex2.printStackTrace();
        }
        return this;
    }
    
    public Object getHandle() {
        return this.entity;
    }
    
    public int ping() {
        try {
            return this.entity.getClass().getField("ping").getInt(this.entity);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
    
    public void sendCustomPayload(final PacketCustomPayload packet) {
        this.sendCorePacket(packet);
    }
    
    public void hideInventory() {
        try {
            this.inventory_hidden = false;
            final Object stack = InventoryStack.getCraftVersion(new ItemStack(Material.AIR, 1));
            final Object activeContainer = this.entity.getClass().getField("activeContainer").get(this.entity);
            final Method method = activeContainer.getClass().getMethod("a", (Class<?>[])new Class[0]);
            final List a = (List)method.invoke(activeContainer, new Object[0]);
            final Class<?> container = Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "Container");
            final List stacks = new ArrayList();
            for (int j1 = 0; j1 < a.size(); ++j1) {
                stacks.add(stack);
            }
            final Method hide = this.entity.getClass().getMethod("a", container, List.class);
            hide.invoke(this.entity, InventoryStack.convert(activeContainer, container), stacks);
            this.inventory_hidden = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void unHideInventory() {
        this.inventory_hidden = false;
        this.player.updateInventory();
    }
    
    public boolean isInventoryHidden() {
        return this.inventory_hidden;
    }
    
    public abstract void setInboundQueue() throws CoreException;
    
    public abstract void setOutboundQueue() throws CoreException;
    
    public void sendCorePacket(final ICorePacket packet) {
        this.send_later.add(packet.getHandle());
    }
    
    protected Object handlePacket(Object packet) {
        if (!this.send_later.isEmpty()) {
            final Object old = packet;
            packet = this.send_later.remove(0);
            if (InternalManager.getPacketId(packet) != InternalManager.getPacketId(old)) {
                this.send_replace.add(packet);
            }
            return packet;
        }
        if (!this.send_replace.isEmpty()) {
            packet = this.send_replace.remove(0);
            return packet;
        }
        if (packet != null) {
            try {
                final int id = InternalManager.getPacketId(packet);
                if (id == 56) {
                    packet = PacketManager.callAsyncMapChunkBulk(this.player, new PacketMapChunkBulk(packet)).getHandle();
                }
                else if (id == 51) {
                    packet = PacketManager.callAsyncMapChunk(this.player, new PacketMapChunk(packet)).getHandle();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return packet;
    }
    
    public Object newEmptyPacket() throws Exception {
        return Class.forName("net.minecraft.server." + InternalManager.getServerVersion() + "Packet0KeepAlive").getConstructor(Integer.TYPE).newInstance(1);
    }
    
    public class PriorityQueue extends ArrayList
    {
        private static final long serialVersionUID = 927895363924203624L;
        
        public boolean add(final Object packet) {
            if (IEntityPlayer.this.isInventoryHidden()) {
                final int id = InternalManager.getPacketId(packet);
                if (id == 103 || id == 104) {
                    return false;
                }
            }
            return super.add(packet);
        }
        
        public Object remove(final int index) {
            final Object packet = IEntityPlayer.this.handlePacket(super.remove(index));
            if (packet == null) {
                try {
                    return IEntityPlayer.this.newEmptyPacket();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return packet;
        }
    }
}
