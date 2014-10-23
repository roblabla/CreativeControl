package me.FurH.CreativeControl.listener;

import org.bukkit.event.entity.*;
import me.FurH.CreativeControl.*;
import org.bukkit.block.*;
import org.bukkit.event.*;
import java.util.*;
import org.bukkit.event.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.manager.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.configuration.*;
import org.bukkit.event.player.*;
import org.bukkit.*;

public class CreativeMiscListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onExplosionPrime(final ExplosionPrimeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getEntity() instanceof TNTPrimed) {
            final TNTPrimed tnt = (TNTPrimed)e.getEntity();
            final Block b = tnt.getLocation().getBlock();
            final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
            if (config.world_exclude) {
                return;
            }
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (config.misc_tnt && manager.isprotected(b, true) != null) {
                this.removeIgnition(b);
                b.setType(Material.TNT);
                e.setCancelled(true);
            }
        }
    }
    
    private void removeIgnition(final Block b) {
        final HashSet<Integer> blocks = new HashSet<Integer>(Arrays.asList(10, 11, 27, 28, 51, 69, 70, 72, 73, 74, 75, 76, 77, 55, 331, 356));
        final int x = b.getX();
        final int y = b.getY();
        final int z = b.getZ();
        final int radius = 2;
        final int minX = x - radius;
        final int minY = y - radius;
        final int minZ = z - radius;
        final int maxX = x + radius;
        final int maxY = y + radius;
        final int maxZ = z + radius;
        for (int counterX = minX; counterX < maxX; ++counterX) {
            for (int counterY = minY; counterY < maxY; ++counterY) {
                for (int counterZ = minZ; counterZ < maxZ; ++counterZ) {
                    final Block block = b.getWorld().getBlockAt(counterX, counterY, counterZ);
                    if (blocks.contains(block.getTypeId())) {
                        final CreativeBlockManager manager = CreativeControl.getManager();
                        manager.unprotect(block);
                        block.breakNaturally();
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Block b = e.getBlock();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (config.misc_fire) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (manager.isprotected(b, true) != null) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBurn(final BlockBurnEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Block b = e.getBlock();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (config.misc_fire) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (manager.isprotected(b, true) != null) {
                this.removeFire(b);
                e.setCancelled(true);
            }
        }
    }
    
    private void removeFire(final Block b) {
        final int x = b.getX();
        final int y = b.getY();
        final int z = b.getZ();
        final int radius = 5;
        final int minX = x - radius;
        final int minY = y - radius;
        final int minZ = z - radius;
        final int maxX = x + radius;
        final int maxY = y + radius;
        final int maxZ = z + radius;
        for (int counterX = minX; counterX < maxX; ++counterX) {
            for (int counterY = minY; counterY < maxY; ++counterY) {
                for (int counterZ = minZ; counterZ < maxZ; ++counterZ) {
                    final Block block = b.getWorld().getBlockAt(counterX, counterY, counterZ);
                    if (block.getType() == Material.FIRE) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockFade(final BlockFadeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Block b = e.getBlock();
        final Material type = b.getType();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (type == Material.ICE && config.misc_ice) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (manager.isprotected(b, true) != null) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockFromTo(final BlockFromToEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Block b = e.getBlock();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (config.misc_liquid) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if ((b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) && manager.isprotected(b, true) != null) {
                b.setType(Material.STATIONARY_WATER);
                e.setCancelled(true);
            }
            if ((b.getType() == Material.LAVA || b.getType() == Material.STATIONARY_LAVA) && manager.isprotected(b, true) != null) {
                b.setType(Material.STATIONARY_LAVA);
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBucketFill(final PlayerBucketFillEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final Block b = e.getBlockClicked();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (config.misc_liquid) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (config.block_ownblock) {
                final CreativeBlockData data = manager.isprotected(b, true);
                if (data != null) {
                    if (manager.isAllowed(p, data)) {
                        manager.unprotect(b);
                    }
                    else {
                        final Communicator com = CreativeControl.plugin.getCommunicator();
                        final CreativeMessages messages = CreativeControl.getMessages();
                        com.msg((CommandSender)p, messages.blockmanager_belongs, data.owner);
                        e.setCancelled(true);
                    }
                }
            }
            else if (config.block_nodrop) {
                manager.unprotect(b);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final Material bucket = e.getBucket();
        final Block bDown = e.getBlockClicked();
        final Block b = e.getBlockClicked().getRelative(e.getBlockFace());
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.misc_liquid && (bucket == Material.WATER_BUCKET || bucket == Material.LAVA_BUCKET || bucket == Material.BUCKET || bucket == Material.MILK_BUCKET)) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (config.block_ownblock) {
                final CreativeBlockData data = manager.isprotected(bDown, true);
                if (data != null) {
                    if (manager.isAllowed(p, data)) {
                        if (bucket == Material.WATER_BUCKET) {
                            b.setType(Material.STATIONARY_WATER);
                        }
                        else if (bucket == Material.LAVA_BUCKET) {
                            b.setType(Material.STATIONARY_LAVA);
                        }
                        manager.protect(p, b);
                    }
                    else {
                        final Communicator com = CreativeControl.plugin.getCommunicator();
                        final CreativeMessages messages = CreativeControl.getMessages();
                        com.msg((CommandSender)p, messages.blockmanager_belongs, data.owner);
                        e.setCancelled(true);
                    }
                }
            }
            if (config.block_nodrop) {
                if (bucket == Material.WATER_BUCKET) {
                    b.setType(Material.STATIONARY_WATER);
                }
                else if (bucket == Material.LAVA_BUCKET) {
                    b.setType(Material.STATIONARY_LAVA);
                }
                manager.protect(p, b);
            }
        }
    }
}
