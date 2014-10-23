package me.FurH.CreativeControl.core.internals;

import java.util.concurrent.*;
import me.FurH.CreativeControl.core.packets.objects.*;
import me.FurH.CreativeControl.core.packets.*;
import me.FurH.CreativeControl.core.reflection.*;
import java.lang.reflect.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;

public class MCPCEntityPlayer extends IEntityPlayer
{
    public void setInboundQueue() throws CoreException {
        final Queue newSyncPackets = new ConcurrentLinkedQueue() {
            private static final long serialVersionUID = 7299839519835756010L;
            
            public boolean add(final Object packet) {
                try {
                    final String name = packet.getClass().getSimpleName();
                    final int id = InternalManager.getPacketId(name);
                    if (id == 250) {
                        PacketManager.callAsyncCustomPayload(MCPCEntityPlayer.this.player, new PacketCustomPayload(packet));
                    }
                    else if (id == 204) {
                        PacketManager.callAsyncClientSettings(MCPCEntityPlayer.this.player);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                return super.add(packet);
            }
        };
        for (final Field field : this.networkManager.getClass().getFields()) {
            if (field.getType().equals(Queue.class)) {
                final Queue syncPackets = (Queue)ReflectionUtils.getPrivateField(this.networkManager, field.getName());
                newSyncPackets.addAll(syncPackets);
                ReflectionUtils.setFinalField(this.networkManager, field.getName(), newSyncPackets);
            }
        }
    }
    
    public void setOutboundQueue() throws CoreException {
        for (final Field field : this.networkManager.getClass().getFields()) {
            if (field.getType().equals(List.class)) {
                final List newhighPriorityQueue = Collections.synchronizedList((List<Object>)new PriorityQueue(/*this*/));
                final List highPriorityQueue = (List)ReflectionUtils.getPrivateField(this.networkManager, field.getName());
                if (highPriorityQueue != null) {
                    newhighPriorityQueue.addAll(highPriorityQueue);
                    highPriorityQueue.clear();
                }
                ReflectionUtils.setFinalField(this.networkManager, field.getName(), newhighPriorityQueue);
            }
        }
    }
}
