package me.FurH.CreativeControl.core.player;

import java.util.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.core.internals.*;
import org.bukkit.block.*;
import org.bukkit.*;

public class PlayerUtils
{
    public static int getPingAverage() throws CoreException {
        final List<Integer> pings = new ArrayList<Integer>();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            final int ping = getPing(p);
            if (ping > 0) {
                pings.add(ping);
            }
        }
        return getAverage(pings.toArray(new Integer[0]));
    }
    
    public static int getAverage(final Integer[] values) {
        int sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum / values.length;
    }
    
    public static int getPing(final Player p) throws CoreException {
        return InternalManager.getEntityPlayer(p, true).ping();
    }
    
    public static boolean isOnline(final Player player) {
        for (final Player p : player.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(player.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isValidName(final String name) {
        return name.replaceAll("[^ a-zA-Z0-9_]", "").equals(name);
    }
    
    public static void toSafeLocation(final Player p) {
        final Location loc = p.getLocation().subtract(0.0, 1.0, 0.0);
        Block block = loc.getBlock();
        if (isSafeBlock(block)) {
            return;
        }
        for (int stack = 256; stack > 0 && !block.getType().isSolid(); block = block.getRelative(BlockFace.DOWN), --stack) {}
        final Block[] arr$;
        final Block[] blocks = arr$ = new Block[] { block, block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.EAST) };
        for (Block process : arr$) {
            for (int stack = 2; stack > 0 && !isSafeBlock(process); --stack) {
                if (isFloorBlock(process.getRelative(BlockFace.DOWN))) {
                    process = process.getRelative(BlockFace.UP);
                }
                else {
                    process = process.getRelative(BlockFace.DOWN);
                }
            }
            if (isSafeBlock(process)) {
                block = process;
                break;
            }
        }
        if (!isSafeBlock(block)) {
            block = getBlockUp(block);
        }
        if (!isSafeBlock(block)) {
            final BlockFace[] arr$2;
            final BlockFace[] faces = arr$2 = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
            for (final BlockFace face : arr$2) {
                Block relative = block.getRelative(face).getRelative(BlockFace.DOWN);
                for (int stack = 256; stack > 0 && isUnsafeBlock(relative); relative = relative.getRelative(face), --stack) {}
                if (!isUnsafeBlock(relative)) {
                    block = relative;
                    break;
                }
            }
        }
        if (!isSafeBlock(block)) {
            block = getBlockUp(block);
        }
        if (!isSafeBlock(block)) {
            p.teleport(p.getWorld().getSpawnLocation());
            return;
        }
        final Location newLoc = block.getRelative(BlockFace.UP).getLocation();
        newLoc.setPitch(p.getLocation().getPitch());
        newLoc.setYaw(p.getLocation().getYaw());
        p.teleport(newLoc);
    }
    
    private static Block getBlockUp(Block block) {
        for (int stack = 256; stack > 0 && !isSafeBlock(block); --stack) {
            if (isFloorBlock(block.getRelative(BlockFace.DOWN))) {
                block = block.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
            }
            else {
                block = block.getRelative(BlockFace.DOWN);
            }
        }
        return block;
    }
    
    private static boolean isSafeBlock(final Block block) {
        return block.getType() != Material.AIR && !isUnsafeBlock(block) && !block.getRelative(BlockFace.UP).getType().isSolid() && !isUnsafeBlock(block.getRelative(BlockFace.UP)) && !block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().isSolid() && !isUnsafeBlock(block.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
    }
    
    private static boolean isUnsafeBlock(final Block block) {
        return block.getTypeId() == 10 || block.getTypeId() == 11 || block.getTypeId() == 51 || block.getTypeId() == 119;
    }
    
    private static boolean isFloorBlock(final Block block) {
        return block.getType() != Material.AIR;
    }
}
