package me.FurH.CreativeControl.core.internals;

import me.FurH.CreativeControl.core.cache.*;
import java.util.regex.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.*;
import org.bukkit.plugin.java.*;
import me.FurH.CreativeControl.core.exceptions.*;

public class InternalManager extends ClassLoader
{
    private static final CoreSafeCache<String, Integer> packets;
    private static CoreSafeCache<String, IEntityPlayer> entities;
    private static String version;
    private static final Pattern brand;
    
    public static void setup(final boolean useEmpty) {
        if (useEmpty) {
            return;
        }
        if (!isNettyEnabled()) {
            return;
        }
        final Plugin protocol = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocol == null || !protocol.isEnabled()) {
            Thread.dumpStack();
            System.out.println("You must have ProtocolLib installed to use with Spigot!");
        }
    }
    
    public static int getPacketId(final Object packet) {
        return getPacketId(packet.getClass().getSimpleName());
    }
    
    public static int getPacketId(final String packet) {
        if (InternalManager.packets.containsKey(packet)) {
            return InternalManager.packets.get(packet);
        }
        return 0;
    }
    
    public static String getServerVersion() {
        return "".equals(InternalManager.version) ? "" : (InternalManager.version + ".");
    }
    
    public static IEntityPlayer getEntityPlayer(final Player player, final boolean useEmpty) throws CoreException {
        IEntityPlayer entity = null;
        if (InternalManager.entities.containsKey(player.getName())) {
            entity = InternalManager.entities.get(player.getName());
            if (entity instanceof EmptyEntityPlayer && useEmpty) {
                return entity;
            }
            InternalManager.entities.remove(player.getName());
        }
        if (useEmpty) {
            entity = new EmptyEntityPlayer();
        }
        else if (isMcPcPlusEnabled()) {
            entity = new MCPCEntityPlayer();
        }
        else if (isNettyEnabled()) {
            entity = new ProtocolEntityPlayer(CorePlugin.getCorePlugin());
        }
        else {
            entity = new BukkitEntityPlayer();
        }
        entity.setEntityPlayer(player);
        InternalManager.entities.put(player.getName(), entity);
        return entity;
    }
    
    public static void removeEntityPlayer(final Player player) {
        InternalManager.entities.remove(player.getName());
    }
    
    public static boolean isNettyEnabled() {
        return Bukkit.getVersion().toLowerCase().contains("spigot");
    }
    
    public static boolean isMcPcPlusEnabled() {
        return Bukkit.getVersion().toLowerCase().contains("mcpc");
    }
    
    static {
        packets = new CoreSafeCache<String, Integer>();
        InternalManager.entities = new CoreSafeCache<String, IEntityPlayer>();
        InternalManager.version = null;
        brand = Pattern.compile("(v|)[0-9][_.][0-9][_.][R0-9]*");
        final String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String version0 = pkg.substring(pkg.lastIndexOf(46) + 1);
        if (!InternalManager.brand.matcher(version0).matches()) {
            version0 = "";
        }
        InternalManager.version = version0;
        InternalManager.packets.put("Packet0KeepAlive", 0);
        InternalManager.packets.put("Packet1Login", 1);
        InternalManager.packets.put("Packet2Handshake", 2);
        InternalManager.packets.put("Packet3Chat", 3);
        InternalManager.packets.put("Packet4UpdateTime", 4);
        InternalManager.packets.put("Packet5EntityEquipment", 5);
        InternalManager.packets.put("Packet6SpawnPosition", 6);
        InternalManager.packets.put("Packet7UseEntity", 7);
        InternalManager.packets.put("Packet8UpdateHealth", 8);
        InternalManager.packets.put("Packet9Respawn", 9);
        InternalManager.packets.put("Packet10Flying", 10);
        InternalManager.packets.put("Packet11PlayerPosition", 11);
        InternalManager.packets.put("Packet12PlayerLook", 12);
        InternalManager.packets.put("Packet13PlayerLookMove", 13);
        InternalManager.packets.put("Packet14BlockDig", 14);
        InternalManager.packets.put("Packet15Place", 15);
        InternalManager.packets.put("Packet16BlockItemSwitch", 16);
        InternalManager.packets.put("Packet17EntityLocationAction", 17);
        InternalManager.packets.put("Packet18ArmAnimation", 18);
        InternalManager.packets.put("Packet19EntityAction", 19);
        InternalManager.packets.put("Packet20NamedEntitySpawn", 20);
        InternalManager.packets.put("Packet22Collect", 22);
        InternalManager.packets.put("Packet23VehicleSpawn", 23);
        InternalManager.packets.put("Packet24MobSpawn", 24);
        InternalManager.packets.put("Packet25EntityPainting", 25);
        InternalManager.packets.put("Packet26AddExpOrb", 26);
        InternalManager.packets.put("Packet28EntityVelocity", 28);
        InternalManager.packets.put("Packet29DestroyEntity", 29);
        InternalManager.packets.put("Packet30Entity", 30);
        InternalManager.packets.put("Packet31RelEntityMove", 31);
        InternalManager.packets.put("Packet32EntityLook", 32);
        InternalManager.packets.put("Packet33RelEntityMoveLook", 33);
        InternalManager.packets.put("Packet34EntityTeleport", 34);
        InternalManager.packets.put("Packet35EntityHeadRotation", 35);
        InternalManager.packets.put("Packet38EntityStatus", 38);
        InternalManager.packets.put("Packet39AttachEntity", 39);
        InternalManager.packets.put("Packet40EntityMetadata", 40);
        InternalManager.packets.put("Packet41MobEffect", 41);
        InternalManager.packets.put("Packet42RemoveMobEffect", 42);
        InternalManager.packets.put("Packet43SetExperience", 43);
        InternalManager.packets.put("Packet51MapChunk", 51);
        InternalManager.packets.put("Packet52MultiBlockChange", 52);
        InternalManager.packets.put("Packet53BlockChange", 53);
        InternalManager.packets.put("Packet54PlayNoteBlock", 54);
        InternalManager.packets.put("Packet55BlockBreakAnimation", 55);
        InternalManager.packets.put("Packet56MapChunkBulk", 56);
        InternalManager.packets.put("Packet60Explosion", 60);
        InternalManager.packets.put("Packet61WorldEvent", 61);
        InternalManager.packets.put("Packet62NamedSoundEffect", 62);
        InternalManager.packets.put("Packet63WorldParticles", 63);
        InternalManager.packets.put("Packet70Bed", 70);
        InternalManager.packets.put("Packet71Weather", 71);
        InternalManager.packets.put("Packet100OpenWindow", 100);
        InternalManager.packets.put("Packet101CloseWindow", 101);
        InternalManager.packets.put("Packet102WindowClick", 102);
        InternalManager.packets.put("Packet103SetSlot", 103);
        InternalManager.packets.put("Packet104WindowItems", 104);
        InternalManager.packets.put("Packet105CraftProgressBar", 105);
        InternalManager.packets.put("Packet106Transaction", 106);
        InternalManager.packets.put("Packet107SetCreativeSlot", 107);
        InternalManager.packets.put("Packet108ButtonClick", 108);
        InternalManager.packets.put("Packet130UpdateSign", 130);
        InternalManager.packets.put("Packet131ItemData", 131);
        InternalManager.packets.put("Packet132TileEntityData", 132);
        InternalManager.packets.put("Packet200Statistic", 200);
        InternalManager.packets.put("Packet201PlayerInfo", 201);
        InternalManager.packets.put("Packet202Abilities", 202);
        InternalManager.packets.put("Packet203TabComplete", 203);
        InternalManager.packets.put("Packet204LocaleAndViewDistance", 204);
        InternalManager.packets.put("Packet205ClientCommand", 205);
        InternalManager.packets.put("Packet206SetScoreboardObjective", 206);
        InternalManager.packets.put("Packet207SetScoreboardScore", 207);
        InternalManager.packets.put("Packet208SetScoreboardDisplayObjective", 208);
        InternalManager.packets.put("Packet209SetScoreboardTeam", 209);
        InternalManager.packets.put("Packet250CustomPayload", 250);
        InternalManager.packets.put("Packet252KeyResponse", 252);
        InternalManager.packets.put("Packet253KeyRequest", 253);
        InternalManager.packets.put("Packet254GetInfo", 254);
        InternalManager.packets.put("Packet255KickDisconnect", 255);
    }
}
