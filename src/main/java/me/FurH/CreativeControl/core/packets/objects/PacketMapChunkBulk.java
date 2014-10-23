package me.FurH.CreativeControl.core.packets.objects;

import me.FurH.CreativeControl.core.reflection.field.*;
import me.FurH.CreativeControl.core.reflection.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class PacketMapChunkBulk implements ICorePacket
{
    private static IReflectField c;
    private static IReflectField d;
    private static IReflectField a;
    private static IReflectField b;
    private static IReflectField buffer;
    private static IReflectField inflatedBuffers;
    private static IReflectField size;
    private static IReflectField h;
    private Object handle;
    
    public PacketMapChunkBulk(final Object packet) {
        super();
        this.handle = packet;
        try {
            if (PacketMapChunkBulk.c == null) {
                PacketMapChunkBulk.c = ReflectionUtils.getNewReflectField("c", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.d == null) {
                PacketMapChunkBulk.d = ReflectionUtils.getNewReflectField("d", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.a == null) {
                PacketMapChunkBulk.a = ReflectionUtils.getNewReflectField("a", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.b == null) {
                PacketMapChunkBulk.b = ReflectionUtils.getNewReflectField("b", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.buffer == null) {
                PacketMapChunkBulk.buffer = ReflectionUtils.getNewReflectField("buffer", packet.getClass(), true);
            }
            if (PacketMapChunkBulk.inflatedBuffers == null) {
                PacketMapChunkBulk.inflatedBuffers = ReflectionUtils.getNewReflectField("inflatedBuffers", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.h == null) {
                PacketMapChunkBulk.h = ReflectionUtils.getNewReflectField("h", packet.getClass(), false);
            }
            if (PacketMapChunkBulk.size == null) {
                PacketMapChunkBulk.size = ReflectionUtils.getNewReflectField("size", packet.getClass(), true);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public int[] getX() throws CoreException {
        return PacketMapChunkBulk.c.getIntArray(this.handle);
    }
    
    public int[] getZ() throws CoreException {
        return PacketMapChunkBulk.d.getIntArray(this.handle);
    }
    
    public int[] getMaskA() throws CoreException {
        return PacketMapChunkBulk.a.getIntArray(this.handle);
    }
    
    public int[] getMaskB() throws CoreException {
        return PacketMapChunkBulk.b.getIntArray(this.handle);
    }
    
    public byte[] getBuffer() throws CoreException {
        return PacketMapChunkBulk.buffer.getByteArray(this.handle);
    }
    
    public void setBuffer(final byte[] buffer0) throws CoreException {
        PacketMapChunkBulk.buffer.set(buffer0, this.handle);
    }
    
    public byte[][] getInflatedBuffers() throws CoreException {
        return PacketMapChunkBulk.inflatedBuffers.getDoubleByteArray(this.handle);
    }
    
    public void setInflatedBuffers(final byte[][] inflatedBuffers0) throws CoreException {
        PacketMapChunkBulk.inflatedBuffers.set(inflatedBuffers0, this.handle);
    }
    
    public int getCompressedSize() throws CoreException {
        return PacketMapChunkBulk.size.getInt(this.handle);
    }
    
    public void setCompressedSize(final int size0) throws CoreException {
        PacketMapChunkBulk.size.set(size0, this.handle);
    }
    
    public boolean h() throws CoreException {
        return PacketMapChunkBulk.h.getBoolean(this.handle);
    }
    
    public int getPacketId() {
        return 56;
    }
    
    public Object getHandle() {
        return this.handle;
    }
    
    public PacketMapChunkBulk setHandle(final Object handle) {
        this.handle = handle;
        return this;
    }
    
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
    
    static {
        PacketMapChunkBulk.c = null;
        PacketMapChunkBulk.d = null;
        PacketMapChunkBulk.a = null;
        PacketMapChunkBulk.b = null;
        PacketMapChunkBulk.buffer = null;
        PacketMapChunkBulk.inflatedBuffers = null;
        PacketMapChunkBulk.size = null;
        PacketMapChunkBulk.h = null;
    }
}
