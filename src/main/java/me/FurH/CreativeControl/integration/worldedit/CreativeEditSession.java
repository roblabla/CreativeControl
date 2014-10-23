package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.bags.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.bukkit.*;
import me.FurH.CreativeControl.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.manager.*;
import net.coreprotect.*;
import me.botsko.prism.*;
import org.bukkit.*;
import me.botsko.prism.actionlibs.*;
import de.diddiz.LogBlock.config.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import de.diddiz.LogBlock.*;

public class CreativeEditSession extends EditSession
{
    private LocalPlayer player;
    
    public CreativeEditSession(final LocalWorld world, final int maxBlocks, final LocalPlayer player) {
        super(world, maxBlocks);
        this.player = player;
    }
    
    public CreativeEditSession(final LocalWorld world, final int maxBlocks, final BlockBag blockBag, final LocalPlayer player) {
        super(world, maxBlocks, blockBag);
        this.player = player;
    }
    
    public boolean rawSetBlock(final Vector pt, final BaseBlock block) {
        if (!(this.world instanceof BukkitWorld)) {
            return super.rawSetBlock(pt, block);
        }
        final World w = ((BukkitWorld)this.world).getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(w);
        final CreativeBlockManager manager = CreativeControl.getManager();
        final int oldType = w.getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        final byte oldData = w.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
        BlockState oldState = null;
        if (oldType == Material.SIGN_POST.getId() || oldType == Material.SIGN.getId()) {
            oldState = w.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
        }
        final boolean success = super.rawSetBlock(pt, block);
        if (success) {
            this.logBlock(pt, block, oldType, oldData, oldState);
            this.prism(pt, block, oldType, oldData);
            this.coreprotect(pt, block, oldType, oldData);
            if (!config.world_exclude && config.block_worledit) {
                final int newType = block.getType();
                if (newType == 0 || oldType != 0) {
                    manager.unprotect(w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                }
                if (newType != 0) {
                    manager.protect(this.player.getName(), w, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newType);
                }
            }
        }
        return success;
    }
    
    public void coreprotect(final Vector vector, final BaseBlock base_block, final int old_type, final int old_data) {
        final CoreProtectAPI protect = CreativeControl.getCoreProtect();
        if (protect != null) {
            final Block block = ((BukkitWorld)this.player.getWorld()).getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            if (!(this.player.getWorld() instanceof BukkitWorld) || Functions.checkConfig(block.getWorld(), "worldedit") == 0) {
                return;
            }
            final int new_type = block.getTypeId();
            final int new_data = block.getData();
            if (old_type != new_type || old_data != new_data) {
                if (old_type == 0 && new_type > 0) {
                    protect.logPlacement(this.player.getName(), block.getLocation(), block.getTypeId(), block.getData());
                }
                else if (old_type > 0 && new_type > 0) {
                    protect.logPlacement(this.player.getName(), block.getLocation(), block.getTypeId(), block.getData());
                }
                else if (old_type > 0 && new_type == 0) {
                    protect.logRemoval(this.player.getName(), block.getLocation(), block.getTypeId(), block.getData());
                }
            }
        }
    }
    
    public void prism(final Vector pt, final BaseBlock block, final int typeBefore, final byte dataBefore) {
        if (CreativeControl.getPrism()) {
            if (!Prism.config.getBoolean("prism.tracking.world-edit")) {
                return;
            }
            final Location loc = new Location(Bukkit.getWorld(this.player.getWorld().getName()), (double)pt.getBlockX(), (double)pt.getBlockY(), (double)pt.getBlockZ());
            RecordingQueue.addToQueue(ActionFactory.create("world-edit", loc, typeBefore, dataBefore, loc.getBlock().getTypeId(), loc.getBlock().getData(), this.player.getName()));
        }
    }
    
    public void logBlock(final Vector pt, final BaseBlock block, final int typeBefore, final byte dataBefore, final BlockState stateBefore) {
        final Consumer consumer = CreativeControl.getLogBlock();
        if (consumer != null) {
            if (!Config.isLogging(this.player.getWorld().getName(), Logging.WORLDEDIT)) {
                return;
            }
            final Location location = new Location(((BukkitWorld)this.player.getWorld()).getWorld(), (double)pt.getBlockX(), (double)pt.getBlockY(), (double)pt.getBlockZ());
            if (Config.isLogging(location.getWorld().getName(), Logging.SIGNTEXT) && (typeBefore == Material.SIGN_POST.getId() || typeBefore == Material.SIGN.getId())) {
                consumer.queueSignBreak(this.player.getName(), (Sign)stateBefore);
                if (block.getType() != Material.AIR.getId()) {
                    consumer.queueBlockPlace(this.player.getName(), location, block.getType(), (byte)block.getData());
                }
            }
            else if (dataBefore != 0) {
                consumer.queueBlockBreak(this.player.getName(), location, typeBefore, dataBefore);
                if (block.getType() != Material.AIR.getId()) {
                    consumer.queueBlockPlace(this.player.getName(), location, block.getType(), (byte)block.getData());
                }
            }
            else {
                consumer.queueBlock(this.player.getName(), location, typeBefore, block.getType(), (byte)block.getData());
            }
        }
    }
}
