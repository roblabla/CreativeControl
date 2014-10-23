package me.FurH.CreativeControl.integration.worldedit;

import com.sk89q.worldedit.bags.*;
import org.bukkit.*;
import me.FurH.CreativeControl.*;
import com.sk89q.worldedit.*;
import org.bukkit.plugin.*;

public class CreativeEditSessionFactory extends EditSessionFactory
{
    public EditSession getEditSession(final LocalWorld world, final int maxBlocks, final LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, player);
    }
    
    public EditSession getEditSession(final LocalWorld world, final int maxBlocks, final BlockBag blockBag, final LocalPlayer player) {
        return new CreativeEditSession(world, maxBlocks, blockBag, player);
    }
    
    public static void setup() {
        Bukkit.getScheduler().runTaskLater((Plugin)CreativeControl.getPlugin(), (Runnable)new Runnable() {
            @Override
            public void run() {
                WorldEdit.getInstance().setEditSessionFactory((EditSessionFactory)new CreativeEditSessionFactory());
            }
        }, 1L);
    }
}
