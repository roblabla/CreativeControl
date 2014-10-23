package me.FurH.CreativeControl.core.blocks;

import org.bukkit.*;
import java.util.*;
import org.bukkit.block.*;
import org.bukkit.material.*;

public class BlockUtils
{
    public static void removeTypeAround(final Block b, final int radius, final Material material) {
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    final Location center = new Location(b.getWorld(), (double)(b.getX() + x), (double)(b.getY() + y), (double)(b.getZ() + z));
                    if (center.distanceSquared(b.getLocation()) <= radius) {
                        if (center.getBlock().getType() == material) {
                            center.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
    
    public static List<Block> getAttachedBlock(final Block block) {
        final List<Block> blocks = new ArrayList<Block>();
        final BlockFace[] arr$;
        final BlockFace[] faces = arr$ = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
        for (final BlockFace face : arr$) {
            final Block relative = block.getRelative(face);
            if (relative.getState().getData() instanceof Attachable) {
                final Attachable a = (Attachable)relative.getState().getData();
                final Block attached = relative.getRelative(a.getAttachedFace());
                if (attached.getLocation().equals((Object)block.getLocation())) {
                    blocks.add(relative);
                }
            }
            else if (face.equals((Object)BlockFace.UP) && relative.getType() != Material.AIR && !relative.getType().isSolid()) {
                blocks.add(relative);
            }
        }
        return blocks;
    }
}
