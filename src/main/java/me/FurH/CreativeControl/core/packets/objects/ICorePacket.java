package me.FurH.CreativeControl.core.packets.objects;

import me.FurH.CreativeControl.core.exceptions.*;

public interface ICorePacket
{
    int getPacketId() throws CoreException;
    
    String getPacketName();
    
    Object getHandle();
}
