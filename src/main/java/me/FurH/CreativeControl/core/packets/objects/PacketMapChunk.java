package me.FurH.CreativeControl.core.packets.objects;

import me.FurH.CreativeControl.core.reflection.field.*;
import me.FurH.CreativeControl.core.reflection.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class PacketMapChunk implements ICorePacket
{
    private static IReflectField a;
    private static IReflectField b;
    private static IReflectField c;
    private static IReflectField d;
    private static IReflectField buffer;
    private static IReflectField inflatedBuffer;
    private static IReflectField e;
    private static IReflectField size;
    private Object handle;
    
    public PacketMapChunk(final Object packet) {
        super();
        try {
            if (PacketMapChunk.a == null) {
                PacketMapChunk.a = ReflectionUtils.getNewReflectField("a", packet.getClass(), false);
            }
            if (PacketMapChunk.b == null) {
                PacketMapChunk.b = ReflectionUtils.getNewReflectField("b", packet.getClass(), false);
            }
            if (PacketMapChunk.c == null) {
                PacketMapChunk.c = ReflectionUtils.getNewReflectField("c", packet.getClass(), false);
            }
            if (PacketMapChunk.d == null) {
                PacketMapChunk.d = ReflectionUtils.getNewReflectField("d", packet.getClass(), false);
            }
            if (PacketMapChunk.buffer == null) {
                PacketMapChunk.buffer = ReflectionUtils.getNewReflectField("buffer", packet.getClass(), true);
            }
            if (PacketMapChunk.inflatedBuffer == null) {
                PacketMapChunk.inflatedBuffer = ReflectionUtils.getNewReflectField("inflatedBuffer", packet.getClass(), false);
            }
            if (PacketMapChunk.e == null) {
                PacketMapChunk.e = ReflectionUtils.getNewReflectField("e", packet.getClass(), false);
            }
            if (PacketMapChunk.size == null) {
                PacketMapChunk.size = ReflectionUtils.getNewReflectField("size", packet.getClass(), true);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.handle = packet;
    }
    
    public int getX() throws CoreException {
        return PacketMapChunk.a.getInt(this.handle);
    }
    
    public int getZ() throws CoreException {
        return PacketMapChunk.b.getInt(this.handle);
    }
    
    public int getMaskA() throws CoreException {
        return PacketMapChunk.c.getInt(this.handle);
    }
    
    public int getMaskB() throws CoreException {
        return PacketMapChunk.d.getInt(this.handle);
    }
    
    public byte[] getBuffer() throws CoreException {
        return PacketMapChunk.buffer.getByteArray(this.handle);
    }
    
    public void setBuffer(final byte[] buffer0) throws CoreException {
        PacketMapChunk.buffer.set(buffer0, this.handle);
    }
    
    public byte[] getInflatedBuffer() throws CoreException {
        return PacketMapChunk.inflatedBuffer.getByteArray(this.handle);
    }
    
    public void setInflatedBuffer(final byte[] inflatedBuffer0) throws CoreException {
        PacketMapChunk.inflatedBuffer.set(inflatedBuffer0, this.handle);
    }
    
    public boolean e() throws CoreException {
        return PacketMapChunk.e.getBoolean(this.handle);
    }
    
    public int getCompressedSize() throws CoreException {
        return PacketMapChunk.size.getInt(this.handle);
    }
    
    public void setCompresedSize(final int size0) throws CoreException {
        PacketMapChunk.size.set(size0, this.handle);
    }
    
    public int getPacketId() {
        return 51;
    }
    
    public Object getHandle() {
        return this.handle;
    }
    
    public PacketMapChunk setHandle(final Object handle) {
        this.handle = handle;
        return this;
    }
    
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
}
