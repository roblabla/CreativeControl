package me.FurH.CreativeControl.core.internals;

import org.bukkit.plugin.java.*;
import com.comphenix.protocol.*;
import me.FurH.CreativeControl.core.exceptions.*;
import org.bukkit.plugin.*;
import me.FurH.CreativeControl.core.packets.objects.*;
import me.FurH.CreativeControl.core.packets.*;
import com.comphenix.protocol.events.*;

public class ProtocolEntityPlayer extends IEntityPlayer
{
    private static AsynchronousManager manager;
    private static JavaPlugin plugin;
    private static boolean inboudSet;
    private static boolean outboundSet;
    
    public ProtocolEntityPlayer(final JavaPlugin plugin) throws CoreException {
        super();
        ProtocolEntityPlayer.manager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        ProtocolEntityPlayer.plugin = plugin;
    }
    
    public void setInboundQueue() throws CoreException {
        if (ProtocolEntityPlayer.inboudSet) {
            return;
        }
        ProtocolEntityPlayer.inboudSet = true;
        ProtocolEntityPlayer.manager.registerAsyncHandler((PacketListener)new PacketAdapter(ProtocolEntityPlayer.plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, new Integer[] { 204, 250 }) {
            public void onPacketSending(final PacketEvent e) {
                switch (e.getPacketID()) {
                    case 250: {
                        PacketManager.callAsyncCustomPayload(e.getPlayer(), new PacketCustomPayload(e.getPacket().getHandle()));
                        break;
                    }
                    case 204: {
                        PacketManager.callAsyncClientSettings(e.getPlayer());
                        break;
                    }
                }
            }
        });
    }
    
    public void setOutboundQueue() throws CoreException {
        if (ProtocolEntityPlayer.outboundSet) {
            return;
        }
        ProtocolEntityPlayer.outboundSet = true;
        ProtocolEntityPlayer.manager.registerAsyncHandler((PacketListener)new PacketAdapter(ProtocolEntityPlayer.plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, new Integer[] { 56, 51 }) {
            public void onPacketSending(final PacketEvent e) {
                final int id = e.getPacketID();
                if (ProtocolEntityPlayer.this.isInventoryHidden() && (id == 103 || id == 104)) {
                    e.setCancelled(true);
                    return;
                }
                final Object handle = ProtocolEntityPlayer.this.handlePacket(e.getPacket().getHandle());
                if (handle == null) {
                    e.setCancelled(true);
                    return;
                }
                if (id == 56) {
                    e.setPacket(new PacketContainer(56, handle));
                }
                else if (id == 51) {
                    e.setPacket(new PacketContainer(51, handle));
                }
            }
        });
    }
    
    static {
        ProtocolEntityPlayer.inboudSet = false;
        ProtocolEntityPlayer.outboundSet = false;
    }
}
