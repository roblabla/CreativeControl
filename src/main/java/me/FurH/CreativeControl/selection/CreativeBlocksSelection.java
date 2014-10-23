package me.FurH.CreativeControl.selection;

import org.bukkit.command.*;
import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import me.FurH.CreativeControl.core.util.*;
import java.util.*;
import me.FurH.CreativeControl.manager.*;
import me.FurH.CreativeControl.configuration.*;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.worldedit.bukkit.*;

public class CreativeBlocksSelection
{
    private long elapsedTime;
    private Location min;
    private Location max;
    
    public CreativeBlocksSelection() {
        super();
        this.elapsedTime = 0L;
        this.min = null;
        this.max = null;
    }
    
    public boolean allBlocks(final CommandSender sender, final String args, final Type type) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlockManager manager = CreativeControl.getManager();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (!plugin.hasPerm(sender, "Commands.Use.others") && !args.equalsIgnoreCase(sender.getName())) {
            com.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
            return true;
        }
        int area = 0;
        if (!main.selection_usewe || this.getSelection((Player)sender) == null) {
            if (!plugin.left.containsKey(sender) && !plugin.right.containsKey(sender)) {
                com.msg(sender, "&4You must select the area first!", new Object[0]);
                return true;
            }
            final CreativeSelection sel = new CreativeSelection(plugin.left.get(sender), plugin.right.get(sender));
            area = sel.getArea();
            this.min = sel.getStart();
            this.max = sel.getEnd();
        }
        else {
            final Selection sel2 = this.getSelection((Player)sender);
            if (sel2 == null) {
                com.msg(sender, "&4You must select the area first!", new Object[0]);
                return true;
            }
            area = sel2.getArea();
            this.min = sel2.getMinimumPoint();
            this.max = sel2.getMaximumPoint();
        }
        com.msg(sender, "&4{0}&7 blocks selected!", area);
        com.msg(sender, "&7This may take a while...", new Object[0]);
        final long startTimer = System.currentTimeMillis();
        final Player player = (Player)sender;
        final World w = this.min.getWorld();
        final Thread t = new Thread() {
            @Override
            public void run() {
                for (int x = CreativeBlocksSelection.this.min.getBlockX(); x <= CreativeBlocksSelection.this.max.getBlockX(); ++x) {
                    for (int y = CreativeBlocksSelection.this.min.getBlockY(); y <= CreativeBlocksSelection.this.max.getBlockY(); ++y) {
                        for (int z = CreativeBlocksSelection.this.min.getBlockZ(); z <= CreativeBlocksSelection.this.max.getBlockZ(); ++z) {
                            final int id = w.getBlockTypeIdAt(x, y, z);
                            if (id != 0) {
                                final CreativeWorldNodes wconfig = CreativeControl.getWorldNodes(w);
                                if (type == Type.DELALL) {
                                    if (wconfig.block_ownblock) {
                                        final CreativeBlockData data = manager.isprotected(w, x, y, z, id, true);
                                        if (data != null && data.owner.equalsIgnoreCase(args)) {
                                            manager.unprotect(w, x, y, z, id);
                                        }
                                    }
                                    else if (wconfig.block_nodrop && plugin.hasPerm((CommandSender)player, "Commands.NoDrop")) {
                                        manager.unprotect(w, x, y, z, id);
                                    }
                                }
                                else if (type == Type.DELPLAYER) {
                                    if (wconfig.block_ownblock) {
                                        if (args.equalsIgnoreCase(player.getName())) {
                                            CreativeBlocksSelection.this.delPlayer(args, w, x, y, z, id);
                                        }
                                        else if (plugin.hasPerm((CommandSender)player, "OwnBlock.DelPlayer")) {
                                            CreativeBlocksSelection.this.delPlayer(args, w, x, y, z, id);
                                        }
                                    }
                                    else if (wconfig.block_nodrop && plugin.hasPerm((CommandSender)player, "Commands.NoDrop")) {
                                        CreativeBlocksSelection.this.delPlayer(args, w, x, y, z, id);
                                    }
                                }
                                else if (type == Type.DELTYPE) {
                                    if (wconfig.block_ownblock) {
                                        final CreativeBlockData data = manager.isprotected(w, x, y, z, id, true);
                                        if (data != null && data.owner.equalsIgnoreCase(player.getName())) {
                                            CreativeBlocksSelection.this.delType(args, w, x, y, z, id);
                                        }
                                    }
                                    else if (wconfig.block_nodrop && plugin.hasPerm((CommandSender)player, "Commands.NoDrop")) {
                                        CreativeBlocksSelection.this.delType(args, w, x, y, z, id);
                                    }
                                }
                                else if (type == Type.ADD) {
                                    final CreativeBlockData data = manager.isprotected(w, x, y, z, id, true);
                                    if (data == null) {
                                        manager.protect(args, w, x, y, z, id);
                                    }
                                }
                                else if (type == Type.ALLOW) {
                                    final CreativeBlockData data = manager.isprotected(w, x, y, z, id, false);
                                    if (data != null && data.owner.equalsIgnoreCase(player.getName())) {
                                        String mod = args.toLowerCase();
                                        HashSet<String> als = new HashSet<String>();
                                        if (data.allowed != null) {
                                            als = data.allowed;
                                        }
                                        if (mod.startsWith("-")) {
                                            mod = mod.substring(1);
                                            if (als.contains(mod)) {
                                                als.remove(mod);
                                            }
                                        }
                                        else if (!als.contains(mod)) {
                                            als.add(mod);
                                        }
                                        manager.update(data, w, x, y, z);
                                    }
                                }
                                else if (type == Type.TRANSFER) {
                                    final CreativeBlockData data = manager.isprotected(w, x, y, z, id, true);
                                    if (data != null && data.owner.equalsIgnoreCase(player.getName())) {
                                        data.owner = args;
                                        manager.update(data, w, x, y, z);
                                    }
                                }
                            }
                        }
                    }
                }
                CreativeBlocksSelection.this.elapsedTime = System.currentTimeMillis() - startTimer;
                com.msg((CommandSender)player, "&7All blocks processed in &4{0}&7 ms", CreativeBlocksSelection.this.elapsedTime);
            }
        };
        t.setName("CreativeControl Selection Thread");
        t.setPriority(1);
        t.start();
        return true;
    }
    
    public void delPlayer(final String args, final World world, final int x, final int y, final int z, final int type) {
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CreativeBlockData data = manager.isprotected(world, x, y, z, type, true);
        if (data != null && data.owner.equalsIgnoreCase(args)) {
            manager.unprotect(world, x, y, z, type);
        }
    }
    
    public void delType(final String args, final World world, final int x, final int y, final int z, final int id) {
        final CreativeBlockManager manager = CreativeControl.getManager();
        try {
            final int type = Integer.parseInt(args);
            if (id == type) {
                manager.unprotect(world, x, y, z, id);
            }
        }
        catch (Exception ex) {
            final Communicator com = CreativeControl.plugin.getCommunicator();
            com.error(ex, "[TAG] {0} is not a valid number!", args);
        }
    }
    
    public Selection getSelection(final Player p) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        final WorldEditPlugin we = plugin.getWorldEdit();
        if (we != null) {
            return plugin.getWorldEdit().getSelection(p);
        }
        return null;
    }
    
    public enum Type
    {
        DELALL, 
        DELPLAYER, 
        DELTYPE, 
        ADD, 
        ALLOW, 
        TRANSFER;
    }
}
