package me.FurH.CreativeControl.core.packets;

import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.packets.objects.*;
import java.util.*;

public class PacketManager
{
    private static IPacketQueue[] inn250;
    private static IPacketQueue[] inn204;
    private static IPacketQueue[] out056;
    private static IPacketQueue[] out051;
    private static IPacketQueue[] out250;
    
    public static boolean register(final IPacketQueue handler, final int packetId) {
        if (packetId == -250) {
            PacketManager.out250 = addElement(PacketManager.out250, handler);
        }
        else if (packetId == 250) {
            PacketManager.inn250 = addElement(PacketManager.inn250, handler);
        }
        else if (packetId == 204) {
            PacketManager.inn204 = addElement(PacketManager.inn204, handler);
        }
        else if (packetId == 56) {
            PacketManager.out056 = addElement(PacketManager.out056, handler);
        }
        else if (packetId == 51) {
            PacketManager.out051 = addElement(PacketManager.out051, handler);
        }
        return true;
    }
    
    public static boolean unregister(final IPacketQueue handler, final int packetId) {
        if (packetId == -250) {
            PacketManager.out250 = removeElement(PacketManager.out250, handler);
        }
        else if (packetId == 250) {
            PacketManager.inn250 = removeElement(PacketManager.inn250, handler);
        }
        else if (packetId == 204) {
            PacketManager.inn204 = removeElement(PacketManager.inn204, handler);
        }
        else if (packetId == 56) {
            PacketManager.out056 = removeElement(PacketManager.out056, handler);
        }
        else if (packetId == 51) {
            PacketManager.out051 = removeElement(PacketManager.out051, handler);
        }
        return true;
    }
    
    public static boolean callAsyncCustomPayload(final Player player, final PacketCustomPayload packet) {
        for (int j1 = 0; j1 < PacketManager.inn250.length; ++j1) {
            if (!PacketManager.inn250[j1].handleAsyncCustomPayload(player, packet)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean callAsyncClientSettings(final Player player) {
        for (int j1 = 0; j1 < PacketManager.inn204.length; ++j1) {
            if (!PacketManager.inn204[j1].handleAsyncClientSettings(player)) {
                return false;
            }
        }
        return true;
    }
    
    public static PacketMapChunk callAsyncMapChunk(final Player player, final PacketMapChunk object) {
        if (PacketManager.out051.length == 0) {
            return object;
        }
        PacketMapChunk obj = null;
        for (int j1 = 0; j1 < PacketManager.out051.length; ++j1) {
            obj = PacketManager.out051[j1].handleAsyncMapChunk(player, (obj == null) ? object : obj);
        }
        return (obj == null) ? object.setHandle(null) : obj;
    }
    
    public static PacketMapChunkBulk callAsyncMapChunkBulk(final Player player, final PacketMapChunkBulk object) {
        if (PacketManager.out056.length == 0) {
            return object;
        }
        PacketMapChunkBulk obj = null;
        for (int j1 = 0; j1 < PacketManager.out056.length; ++j1) {
            obj = PacketManager.out056[j1].handleAsyncMapChunkBulk(player, (obj == null) ? object : obj);
        }
        return (obj == null) ? object.setHandle(null) : obj;
    }
    
    private static IPacketQueue[] addElement(final IPacketQueue[] source, final IPacketQueue element) {
        final List<IPacketQueue> list = new ArrayList<IPacketQueue>(Arrays.asList(source));
        if (!list.contains(element)) {
            list.add(element);
        }
        return list.toArray(new IPacketQueue[list.size()]);
    }
    
    private static IPacketQueue[] removeElement(final IPacketQueue[] source, final IPacketQueue element) {
        final List<IPacketQueue> list = new ArrayList<IPacketQueue>(Arrays.asList(source));
        final Iterator<IPacketQueue> i = list.iterator();
        while (i.hasNext()) {
            if (i.next() == element) {
                i.remove();
            }
        }
        return list.toArray(new IPacketQueue[list.size()]);
    }
    
    static {
        PacketManager.inn250 = new IPacketQueue[0];
        PacketManager.inn204 = new IPacketQueue[0];
        PacketManager.out056 = new IPacketQueue[0];
        PacketManager.out051 = new IPacketQueue[0];
        PacketManager.out250 = new IPacketQueue[0];
    }
}
