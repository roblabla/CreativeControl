package me.FurH.CreativeControl.core.packets.objects;

import me.FurH.CreativeControl.core.reflection.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class PacketCustomPayload implements ICorePacket
{
    private Object handle;
    private String channel;
    private byte[] data;
    private int length;
    
    public PacketCustomPayload(final Object packet) {
        super();
        this.handle = packet;
        try {
            this.channel = (String)ReflectionUtils.getPrivateField(packet, "tag");
            this.data = (byte[])ReflectionUtils.getPrivateField(packet, "data");
            this.length = ReflectionUtils.getPrivateIntField(packet, "length");
        }
        catch (CoreException ex) {
            ex.printStackTrace();
        }
    }
    
    public PacketCustomPayload() {
        super();
    }
    
    public void setChannel(final String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return this.channel;
    }
    
    public void setData(final byte[] data) {
        this.data = data;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public void setLength(final int length) {
        this.length = length;
    }
    
    public int getLength() {
        return this.length;
    }
    
    public int getPacketId() {
        return 250;
    }
    
    public PacketCustomPayload setHandle(final Object handle) {
        this.handle = handle;
        return this;
    }
    
    public Object getHandle() {
        return this.handle;
    }
    
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
}
