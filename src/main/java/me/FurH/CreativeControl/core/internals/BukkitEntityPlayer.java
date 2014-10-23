package me.FurH.CreativeControl.core.internals;

import java.util.concurrent.*;
import me.FurH.CreativeControl.core.packets.objects.*;
import me.FurH.CreativeControl.core.packets.*;
import me.FurH.CreativeControl.core.reflection.*;
import me.FurH.CreativeControl.core.exceptions.*;
import java.util.*;

public class BukkitEntityPlayer extends IEntityPlayer
{
    public void setInboundQueue() throws CoreException {
        final Queue newSyncPackets = new ConcurrentLinkedQueue() {
            private static final long serialVersionUID = 7299839519835756010L;
            
            public boolean add(final Object packet) {
                try {
                    final int id = InternalManager.getPacketId(packet);
                    if (id == 250) {
                        PacketManager.callAsyncCustomPayload(BukkitEntityPlayer.this.player, new PacketCustomPayload(packet));
                    }
                    else if (id == 204) {
                        PacketManager.callAsyncClientSettings(BukkitEntityPlayer.this.player);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                return super.add(packet);
            }
        };
        final Queue syncPackets = (Queue)ReflectionUtils.getPrivateField(this.networkManager, "inboundQueue");
        newSyncPackets.addAll(syncPackets);
        ReflectionUtils.setFinalField(this.networkManager, "inboundQueue", newSyncPackets);
    }
    
    public void setOutboundQueue() throws CoreException {
        final List newhighPriorityQueue = Collections.synchronizedList((List<Object>)new PriorityQueue(/*this*/));
        final List newlowPriorityQueue = Collections.synchronizedList((List<Object>)new PriorityQueue(/*this*/));
        final List highPriorityQueue = (List)ReflectionUtils.getPrivateField(this.networkManager, "highPriorityQueue");
        final List lowPriorityQueue = (List)ReflectionUtils.getPrivateField(this.networkManager, "lowPriorityQueue");
        if (highPriorityQueue != null) {
            newhighPriorityQueue.addAll(highPriorityQueue);
            highPriorityQueue.clear();
        }
        if (lowPriorityQueue != null) {
            newlowPriorityQueue.addAll(lowPriorityQueue);
            lowPriorityQueue.clear();
        }
        ReflectionUtils.setFinalField(this.networkManager, "highPriorityQueue", newhighPriorityQueue);
        ReflectionUtils.setFinalField(this.networkManager, "lowPriorityQueue", newlowPriorityQueue);
    }
}
