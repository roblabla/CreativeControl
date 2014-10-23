package me.FurH.CreativeControl.listener;

import me.FurH.CreativeControl.core.cache.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.stack.*;
import org.bukkit.command.*;
import org.bukkit.block.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.blacklist.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.manager.*;
import org.bukkit.event.*;
import me.FurH.CreativeControl.core.blocks.*;
import java.util.*;
import org.bukkit.material.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import me.botsko.prism.*;
import me.botsko.prism.actionlibs.*;
import de.diddiz.LogBlock.*;
import net.coreprotect.*;
import me.FurH.CreativeControl.core.*;

public class CreativeBlockListener implements Listener
{
    private CoreLRUCache<String, CoreLRUCache<String, CreativeBlockLimit>> limits;
    
    public CreativeBlockListener() {
        super();
        this.limits = new CoreLRUCache<String, CoreLRUCache<String, CreativeBlockLimit>>();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getPlayer() == null) {
            return;
        }
        final Player p = e.getPlayer();
        final Block b = e.getBlockPlaced();
        final World world = p.getWorld();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final Communicator com = plugin.getCommunicator();
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        if (config.world_exclude) {
            return;
        }
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (!main.events_move && CreativePlayerListener.onPlayerWorldChange(p, false)) {
            e.setCancelled(true);
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            final CreativeItemStack itemStack = new CreativeItemStack(b.getTypeId(), b.getData());
            if (!config.black_place.isEmpty() && blacklist.isBlackListed(config.black_place, itemStack) && !plugin.hasPerm((CommandSender)p, "BlackList.BlockPlace." + b.getTypeId())) {
                com.msg((CommandSender)p, messages.blockplace_cantplace, new Object[0]);
                e.setCancelled(true);
                return;
            }
            if (config.prevent_wither && !plugin.hasPerm((CommandSender)p, "Preventions.Wither") && e.getBlockPlaced().getType() == Material.SKULL && ((world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND && world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.SOUL_SAND && world.getBlockAt(b.getX() + 1, b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND && world.getBlockAt(b.getX() - 1, b.getY() - 1, b.getZ()).getType() == Material.SOUL_SAND) || (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() - 1).getType() == Material.SOUL_SAND && world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() + 1).getType() == Material.SOUL_SAND))) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                e.setCancelled(true);
                return;
            }
            if (config.prevent_snowgolem && !plugin.hasPerm((CommandSender)p, "Preventions.SnowGolem") && (b.getType() == Material.PUMPKIN || b.getType() == Material.JACK_O_LANTERN) && world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.SNOW_BLOCK && world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.SNOW_BLOCK) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                e.setCancelled(true);
                return;
            }
            if (config.prevent_irongolem && !plugin.hasPerm((CommandSender)p, "Preventions.IronGolem") && (b.getType() == Material.PUMPKIN || b.getType() == Material.JACK_O_LANTERN) && world.getBlockAt(b.getX(), b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK && world.getBlockAt(b.getX(), b.getY() - 2, b.getZ()).getType() == Material.IRON_BLOCK && ((world.getBlockAt(b.getX() + 1, b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK && world.getBlockAt(b.getX() - 1, b.getY() - 1, b.getZ()).getType() == Material.IRON_BLOCK) || (world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() + 1).getType() == Material.IRON_BLOCK && world.getBlockAt(b.getX(), b.getY() - 1, b.getZ() - 1).getType() == Material.IRON_BLOCK))) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                e.setCancelled(true);
                return;
            }
            if (config.block_pistons) {
                final BlockFace[] arr$;
                final BlockFace[] faces = arr$ = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
                final int len$ = arr$.length;
                int i$ = 0;
                while (i$ < len$) {
                    final BlockFace face = arr$[i$];
                    final Block relative = b.getRelative(face);
                    if (relative.getType() == Material.PISTON_BASE || relative.getType() == Material.PISTON_EXTENSION || relative.getType() == Material.PISTON_MOVING_PIECE || relative.getType() == Material.PISTON_STICKY_BASE) {
                        final int data = b.getData() ^ 0x7;
                        final BlockFace head = relative.getFace(relative.getRelative((data == 1) ? BlockFace.UP : ((data == 2) ? BlockFace.EAST : ((data == 3) ? BlockFace.WEST : ((data == 4) ? BlockFace.NORTH : BlockFace.SOUTH)))));
                        final Block front = relative.getRelative(head.getOppositeFace());
                        if (front.getLocation().equals((Object)b.getLocation())) {
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    }
                    else {
                        ++i$;
                    }
                }
            }
        }
        final int limit = config.block_minutelimit;
        if (this.isLimitReached(p, limit)) {
            com.msg((CommandSender)p, messages.blockmanager_limit, limit);
            e.setCancelled(true);
            return;
        }
        final CreativeBlockManager manager = CreativeControl.getManager();
        final Block r = e.getBlockReplacedState().getBlock();
        final Block ba = e.getBlockAgainst();
        if (config.block_nodrop) {
            if (config.misc_liquid && r.getType() != Material.AIR) {
                manager.unprotect(r);
            }
            if (p.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)p, "NoDrop.DontSave")) {
                manager.protect(p, b);
            }
        }
        else if (config.block_ownblock) {
            if (config.misc_liquid && r.getType() != Material.AIR) {
                final CreativeBlockData data2 = manager.isprotected(r, true);
                if (data2 != null) {
                    if (!manager.isAllowed(p, data2)) {
                        com.msg((CommandSender)p, messages.blockmanager_belongs, data2.owner);
                        e.setCancelled(true);
                        return;
                    }
                    manager.unprotect(b);
                }
            }
            if (config.block_against) {
                final CreativeBlockData data2 = manager.isprotected(ba, true);
                if (data2 != null && !manager.isAllowed(p, data2)) {
                    com.msg((CommandSender)p, messages.blockmanager_belongs, data2.owner);
                    e.setCancelled(true);
                    return;
                }
            }
            if (p.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)p, "OwnBlock.DontSave")) {
                manager.protect(p, b);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getPlayer() == null) {
            return;
        }
        final Player p = e.getPlayer();
        final Block b = e.getBlock();
        final World world = b.getWorld();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeBlockManager manager = CreativeControl.getManager();
        final Communicator com = plugin.getCommunicator();
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        if (config.world_exclude) {
            return;
        }
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (!main.events_move && CreativePlayerListener.onPlayerWorldChange(p, false)) {
            e.setCancelled(true);
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            if (config.prevent_bedrock && !plugin.hasPerm((CommandSender)p, "Preventions.BreakBedRock") && b.getType() == Material.BEDROCK && b.getY() < 1) {
                com.msg((CommandSender)p, messages.blockbreak_survival, new Object[0]);
                e.setCancelled(true);
                return;
            }
            final CreativeItemStack itemStack = new CreativeItemStack(b.getTypeId(), b.getData());
            if (!config.black_break.isEmpty() && blacklist.isBlackListed(config.black_break, itemStack) && !plugin.hasPerm((CommandSender)p, "BlackList.BlockBreak." + b.getTypeId())) {
                com.msg((CommandSender)p, messages.blockbreak_cantbreak, new Object[0]);
                e.setCancelled(true);
                return;
            }
        }
        final List<Block> attached = new ArrayList<Block>();
        if (config.block_nodrop || config.block_ownblock) {
            if (config.block_attach) {
                if (!config.block_physics && this.isPhysics(b.getRelative(BlockFace.UP))) {
                    attached.add(b.getRelative(BlockFace.UP));
                }
                attached.addAll(BlockUtils.getAttachedBlock(b));
            }
            attached.add(b);
        }
        if (config.block_nodrop) {
            for (final Block block : attached) {
                final CreativeBlockData data = manager.isprotected(block, false);
                if (data != null) {
                    this.process(config, e, block, p);
                }
            }
        }
        else if (config.block_ownblock) {
            for (final Block block : attached) {
                final CreativeBlockData data = manager.isprotected(block, true);
                if (data != null) {
                    if (!manager.isAllowed(p, data)) {
                        com.msg((CommandSender)p, messages.blockmanager_belongs, data.owner);
                        e.setCancelled(true);
                        break;
                    }
                    this.process(config, e, block, p);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPistonExtend(final BlockPistonExtendEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final World world = e.getBlock().getWorld();
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        if (config.world_exclude) {
            return;
        }
        if (config.block_pistons) {
            for (final Block b : e.getBlocks()) {
                if (b.getType() != Material.AIR && manager.isprotected(b, true) != null) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Block b = e.getBlock();
        final World world = b.getWorld();
        if (b.getType() == Material.AIR) {
            return;
        }
        if (!e.isSticky()) {
            return;
        }
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        if (config.world_exclude) {
            return;
        }
        if (config.block_pistons) {
            BlockFace direction = null;
            final MaterialData data = b.getState().getData();
            if (data instanceof PistonBaseMaterial) {
                direction = ((PistonBaseMaterial)data).getFacing();
            }
            if (direction == null) {
                return;
            }
            final Block moved = b.getRelative(direction, 2);
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (manager.isprotected(moved, true) != null) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityChangeBlock(final EntityChangeBlockEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getEntity() instanceof FallingBlock) {
            final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getBlock().getWorld());
            if (config.world_exclude) {
                return;
            }
            if (!config.block_physics) {
                return;
            }
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (manager.isprotected(e.getBlock(), true) != null) {
                e.setCancelled(true);
            }
        }
    }
    
    private boolean isPhysics(final Block block) {
        return block.getType() == Material.SAND || block.getType() == Material.GRAVEL || block.getType() == Material.CACTUS || block.getType() == Material.SUGAR_CANE_BLOCK || block.getType() == Material.ANVIL;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockFromTo(final BlockFromToEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final CreativeBlockManager manager = CreativeControl.getManager();
        final Block block = e.getBlock();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getBlock().getWorld());
        if (config.world_exclude) {
            return;
        }
        if (e.getBlock().getType() == Material.DRAGON_EGG) {
            if (config.block_nodrop || config.block_ownblock) {
                final CreativeBlockData data = manager.isprotected(block, false);
                if (data != null) {
                    e.setCancelled(true);
                }
            }
            return;
        }
        if (e.getBlock().getType() != Material.WATER && e.getBlock().getType() != Material.STATIONARY_WATER) {
            return;
        }
        if (!this.isWaterAffected(e.getToBlock())) {
            return;
        }
        if (!config.block_water) {
            return;
        }
        if (config.block_nodrop) {
            final CreativeBlockData data = manager.isprotected(block, false);
            if (data != null) {
                block.setType(Material.AIR);
            }
        }
        else if (config.block_ownblock) {
            final CreativeBlockData data = manager.isprotected(block, true);
            if (data != null) {
                e.setCancelled(true);
            }
        }
    }
    
    private boolean isWaterAffected(final Block block) {
        return block.getTypeId() == 6 || block.getTypeId() == 30 || block.getTypeId() == 31 || block.getTypeId() == 37 || block.getTypeId() == 38 || block.getTypeId() == 39 || block.getTypeId() == 40 || block.getTypeId() == 50 || block.getTypeId() == 78 || block.getTypeId() == 390 || block.getTypeId() == 397 || block.getTypeId() == 69 || block.getTypeId() == 75 || block.getTypeId() == 76 || block.getTypeId() == 77 || block.getTypeId() == 131 || block.getTypeId() == 143 || block.getTypeId() == 55 || block.getTypeId() == 404 || block.getTypeId() == 356 || block.getTypeId() == 27 || block.getTypeId() == 28 || block.getTypeId() == 66 || block.getTypeId() == 157;
    }
    
    private void log(final Player p, final Block b) {
        final Consumer consumer = CreativeControl.getLogBlock();
        if (consumer != null) {
            consumer.queueBlockBreak(p.getName(), b.getState());
        }
        if (CreativeControl.getPrism()) {
            RecordingQueue.addToQueue(ActionFactory.create("block-break", b, p.getName()));
        }
        final CoreProtectAPI protect = CreativeControl.getCoreProtect();
        if (protect != null) {
            protect.logRemoval(p.getName(), b.getLocation(), b.getTypeId(), b.getData());
        }
    }
    
    private void process(final CreativeWorldNodes config, final BlockBreakEvent e, final Block b, final Player p) {
        if (!e.isCancelled()) {
            final CreativeMessages messages = CreativeControl.getMessages();
            final CreativeBlockManager manager = CreativeControl.getManager();
            final Communicator com = CreativeControl.plugin.getCommunicator();
            if (config.block_creative && !p.getGameMode().equals((Object)GameMode.CREATIVE)) {
                com.msg((CommandSender)p, messages.blockbreak_cantbreak, new Object[0]);
                e.setCancelled(true);
                return;
            }
            manager.unprotect(b);
            this.log(p, b);
            e.setExpToDrop(0);
            b.setType(Material.AIR);
            if (!p.getGameMode().equals((Object)GameMode.CREATIVE)) {
                com.msg((CommandSender)p, messages.blockbreak_creativeblock, new Object[0]);
            }
        }
    }
    
    public boolean isLimitReached(final Player player, final int limit) {
        if (limit < 0) {
            return false;
        }
        if (CorePlugin.hasPermS((CommandSender)player, "Preventions.MinuteLimit")) {
            return false;
        }
        if (!this.limits.containsKey(player.getName())) {
            this.limits.put(player.getName(), new CoreLRUCache<String, CreativeBlockLimit>());
        }
        final CoreLRUCache<String, CreativeBlockLimit> world = this.limits.get(player.getName());
        if (!world.containsKey(player.getWorld().getName())) {
            world.put(player.getWorld().getName(), new CreativeBlockLimit());
        }
        final CreativeBlockLimit data = world.get(player.getWorld().getName());
        if (data.isExpired()) {
            data.reset();
        }
        data.increment();
        world.put(player.getWorld().getName(), data);
        this.limits.put(player.getName(), world);
        return data.getPlaced() > limit;
    }
}
