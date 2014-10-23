package me.FurH.CreativeControl.commands;

import org.bukkit.command.*;
import me.FurH.CreativeControl.*;
import org.bukkit.entity.*;
import me.FurH.CreativeControl.core.inventory.*;
import me.FurH.CreativeControl.core.exceptions.*;
import me.FurH.CreativeControl.configuration.*;
import org.bukkit.plugin.*;
import me.FurH.CreativeControl.util.*;
import me.FurH.CreativeControl.data.friend.*;
import me.FurH.CreativeControl.database.*;
import me.FurH.CreativeControl.manager.*;
import java.util.*;
import me.FurH.CreativeControl.database.extra.*;
import org.bukkit.*;
import me.FurH.CreativeControl.selection.*;
import com.sk89q.worldedit.bukkit.selections.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.region.*;

public class CreativeCommands implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (args.length <= 0) {
            this.msg(sender, "&8[&4CreativeControl&8]&7: &8CreativeControl &4{0} &8by &4FurmigaHumana", plugin.getDescription().getVersion());
            this.msg(sender, "&8[&4CreativeControl&8]&7: Type '&4/cc help&7' to see the command list", new Object[0]);
            return true;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("tool")) {
                return this.toolCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("status")) {
                return this.statusCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("del")) {
                return this.delCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("add")) {
                return this.addCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("admin")) {
                return this.onAdminCommand(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("check")) {
                return this.checkCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("cleanup")) {
                return this.cleanupCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("region")) {
                return this.regionCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("sel")) {
                return this.selCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("friend") || args[0].equalsIgnoreCase("f")) {
                return this.friendCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("reload")) {
                return this.reloadCmd(sender, cmd, string, args);
            }
            if (args[0].equalsIgnoreCase("set")) {
                return this.onSetArmorCommand(sender, cmd, string, args);
            }
        }
        this.msg(sender, "&4/cc tool <add/del> &8-&7 Manualy unprotect/protect blocks", new Object[0]);
        this.msg(sender, "&4/cc status &8-&7 Simple cache and database status", new Object[0]);
        this.msg(sender, "&4/cc <add/del> &8-&7 Protect/unprotect blocks inside the selection", new Object[0]);
        this.msg(sender, "&4/cc admin migrator &8-&7 Migrate the database to others types", new Object[0]);
        this.msg(sender, "&4/cc check <status/player> &8-&7 Get player gamemode data", new Object[0]);
        this.msg(sender, "&4/cc cleanup <all/type/player/world/corrupt> &8-&7 Clean the database", new Object[0]);
        this.msg(sender, "&4/cc region <create/remove> &8-&7 Create or remove gamemode regions", new Object[0]);
        this.msg(sender, "&4/cc sel expand <up/down/ver> &8-&7 Expand the current selection", new Object[0]);
        this.msg(sender, "&4/cc friend <add/remove/list/allow/transf> &8-&7 Friend list manager", new Object[0]);
        this.msg(sender, "&4/cc reload &8-&7 Full reload of the plugin", new Object[0]);
        return true;
    }
    
    public boolean onSetArmorCommand(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return false;
        }
        if (!plugin.hasPerm(sender, "Commands.SetArmor")) {
            this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
            return true;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("armor")) {
            final Player p = (Player)sender;
            try {
                config.set(config.getSettingsFile(), "CreativeArmor.Helmet", InventoryStack.getStringFromItemStack(p.getInventory().getHelmet()));
                config.set(config.getSettingsFile(), "CreativeArmor.Chestplate", InventoryStack.getStringFromItemStack(p.getInventory().getChestplate()));
                config.set(config.getSettingsFile(), "CreativeArmor.Leggings", InventoryStack.getStringFromItemStack(p.getInventory().getLeggings()));
                config.set(config.getSettingsFile(), "CreativeArmor.Boots", InventoryStack.getStringFromItemStack(p.getInventory().getBoots()));
            }
            catch (CoreException ex) {
                plugin.error(ex, "Failed to set the creative armor data", new Object[0]);
            }
            config.updateConfig();
            config.load();
            this.msg(sender, "&7Creative armor defined as your current armor", new Object[0]);
            return true;
        }
        this.msg(sender, "&4/cc set armor &8-&7 Set your current armor as the creative armor", new Object[0]);
        return true;
    }
    
    public boolean onAdminCommand(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Admin")) {
            this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
            return true;
        }
        if (args.length > 2 && (args[2].equalsIgnoreCase(">sqlite") || args[2].equalsIgnoreCase(">mysql") || args[2].equalsIgnoreCase(">h2"))) {
            CreativeSQLMigrator migrator = null;
            if (sender instanceof Player) {
                migrator = new CreativeSQLMigrator(plugin, (Player)sender, args[2]);
            }
            else {
                migrator = new CreativeSQLMigrator(plugin, null, args[2]);
            }
            if (CreativeSQLMigrator.lock) {
                this.msg(sender, "&4The migrator is already running!", new Object[0]);
            }
            else {
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)plugin, (Runnable)migrator);
            }
            return true;
        }
        this.msg(sender, "&4/cc admin migrator >sqlite &8-&7 Convert actual database to SQLite", new Object[0]);
        this.msg(sender, "&4/cc admin migrator >mysql &8-&7 Convert actual database to MySQL", new Object[0]);
        this.msg(sender, "&4/cc admin migrator >h2 &8-&7 Convert actual database to H2", new Object[0]);
        return true;
    }
    
    public boolean friendCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativePlayerFriends friends = CreativeControl.getFriends();
        final CreativeBlocksSelection selection = CreativeControl.getSelector();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (args.length > 3) {
            if (args[1].equalsIgnoreCase("transfer") && !args[2].equals("all")) {
                if (!plugin.hasPerm(sender, "Commands.Friend.transfer.all")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                this.msg(sender, "&7Loading...", new Object[0]);
                final CreativeBlockManager manager = CreativeControl.getManager();
                final int newOwner = db.getPlayerId(args[3].toLowerCase());
                final int oldOwner = db.getPlayerId(sender.getName().toLowerCase());
                for (final World world : Bukkit.getWorlds()) {
                    db.queue("UPDATE `" + db.prefix + "blocks_" + world.getName() + "` SET owner = '" + newOwner + "' WHERE owner = '" + oldOwner + "'");
                }
                manager.clear();
                this.msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.", new Object[0]);
                return true;
            }
        }
        else if (args.length > 2) {
            if (args[1].equalsIgnoreCase("add")) {
                if (!plugin.hasPerm(sender, "Commands.Friend.add")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (friends.getFriends(sender.getName()) == null) {
                    HashSet<String> list = CreativeUtil.toStringHashSet(args[2], ", ");
                    friends.saveFriends(sender.getName(), list);
                    this.msg(sender, "&4{0}&7 was added to your friendlist!", args[2]);
                    list.clear();
                    list = null;
                    return true;
                }
                HashSet<String> list = friends.getFriends(sender.getName());
                if (list.contains(args[2])) {
                    this.msg(sender, "&4{0}&7 is in your friendlist already!", args[2]);
                }
                else {
                    list.add(args[2]);
                    friends.saveFriends(sender.getName(), list);
                    this.msg(sender, "&4{0}&7 was added to your friendlist!", args[2]);
                }
                list.clear();
                list = null;
                return true;
            }
            else if (args[1].equalsIgnoreCase("list")) {
                if (!plugin.hasPerm(sender, "Commands.Friend.list")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (friends.getFriends(args[2]) == null || friends.getFriends(args[2]).isEmpty()) {
                    this.msg(sender, "&4{0}&7 has no friends :(", args[2]);
                    return true;
                }
                final String list2 = friends.getFriends(args[2]).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
                this.msg(sender, "&4{0}'s &7friends&8: &7{1}", args[2], list2);
                return true;
            }
            else if (args[1].equalsIgnoreCase("remove")) {
                if (!plugin.hasPerm(sender, "Commands.Friend.remove")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (friends.getFriends(sender.getName()) == null) {
                    this.msg(sender, "&7Your friendlist is empty!", new Object[0]);
                    return true;
                }
                final HashSet<String> list = friends.getFriends(sender.getName());
                if (list.contains(args[2])) {
                    list.remove(args[2]);
                    friends.saveFriends(sender.getName(), list);
                    this.msg(sender, "&4{0}&7 has been removed from your friendlist!", args[2]);
                    return true;
                }
                this.msg(sender, "&4{0}&7 is not in your friendlist!", args[2]);
                return true;
            }
            else if (args[1].equalsIgnoreCase("allow")) {
                if (!(sender instanceof Player)) {
                    this.msg(sender, "&4This command can't be used here!", new Object[0]);
                    return false;
                }
                if (!plugin.hasPerm(sender, "Commands.Friend.allow")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.ALLOW);
                return true;
            }
            else if (args[1].equalsIgnoreCase("transfer")) {
                if (!(sender instanceof Player)) {
                    this.msg(sender, "&4This command can't be used here!", new Object[0]);
                    return false;
                }
                if (!plugin.hasPerm(sender, "Commands.Friend.transfer")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (!args[2].equals("all")) {
                    selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.TRANSFER);
                    return true;
                }
            }
        }
        else if (args.length > 1 && args[1].equalsIgnoreCase("list")) {
            if (!plugin.hasPerm(sender, "Commands.Friend.list")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            if (friends.getFriends(sender.getName()) == null || friends.getFriends(sender.getName()).isEmpty()) {
                this.msg(sender, "&7Your friendlist is empty!", new Object[0]);
                return true;
            }
            final String list2 = friends.getFriends(sender.getName()).toString().replaceAll("\\[", "&4[&7").replaceAll("\\]", "&4]&7").replaceAll("\\,", "&4,&7");
            this.msg(sender, "&7You friends&8: &7{1}", sender.getName(), list2);
            return true;
        }
        this.msg(sender, "&4/cc f list [<player>] &8-&7 List all player friends", new Object[0]);
        this.msg(sender, "&4/cc f add <player> &8-&7 Add a new player to your friend list", new Object[0]);
        this.msg(sender, "&4/cc f remove <player> &8-&7 Remove a player from your friend list", new Object[0]);
        this.msg(sender, "&4/cc f allow <player> &8-&7 Allow a player in your block selection", new Object[0]);
        this.msg(sender, "&4/cc f transfer <player/all> [<player>] &8-&7 Transfer the block ownship of all your blocks or from the selection", new Object[0]);
        return true;
    }
    
    public boolean checkCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("player")) {
                if (!plugin.hasPerm(sender, "Commands.Check.player")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                final Player player = Bukkit.getPlayer(args[2]);
                if (player != null) {
                    this.msg(sender, "&7{0} has gamemode &4{1}", player.getName(), player.getGameMode().toString().toLowerCase());
                }
                else {
                    this.msg(sender, "&7{0} &4is not&7 online!", args[2]);
                }
                return true;
            }
        }
        else if (args.length > 1 && args[1].equalsIgnoreCase("status")) {
            if (!plugin.hasPerm(sender, "Commands.Check.status")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            int creative = 0;
            int survival = 0;
            for (final Player players : Bukkit.getOnlinePlayers()) {
                if (players.getGameMode().equals((Object)GameMode.CREATIVE)) {
                    ++creative;
                }
                if (players.getGameMode().equals((Object)GameMode.SURVIVAL)) {
                    ++survival;
                }
            }
            this.msg(sender, "&7Here are: &4{0}&7 Survival and &4{1}&7 Creative players", survival, creative);
            return true;
        }
        this.msg(sender, "&4/cc check status &8-&7 Check the player gamemodes", new Object[0]);
        this.msg(sender, "&4/cc check player <player> &8-&7 Get the player gamemode", new Object[0]);
        return true;
    }
    
    public boolean addCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlocksSelection selection = CreativeControl.getSelector();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return false;
        }
        if (args.length > 2 && args[1].equalsIgnoreCase("player")) {
            if (!plugin.hasPerm(sender, "Commands.Add.player")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.ADD);
            return true;
        }
        else {
            if (!plugin.hasPerm(sender, "Commands.Add")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            selection.allBlocks(sender, sender.getName(), CreativeBlocksSelection.Type.ADD);
            return true;
        }
    }
    
    public boolean cleanupCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final CreativeBlockManager manager = CreativeControl.getManager();
        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("type")) {
                if (!plugin.hasPerm(sender, "Commands.Cleanup.type")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                for (final World world : Bukkit.getWorlds()) {
                    db.queue("DELETE FROM `" + db.prefix + "blocks_" + world.getName() + "` WHERE type = '" + args[2] + "'");
                }
                manager.clear();
                this.msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.", new Object[0]);
                return true;
            }
            else if (args[1].equalsIgnoreCase("player")) {
                if (!plugin.hasPerm(sender, "Commands.Cleanup.player")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                for (final World world : Bukkit.getWorlds()) {
                    db.queue("DELETE FROM `" + db.prefix + "blocks_" + world.getName() + "` WHERE owner = '" + db.getPlayerId(args[2]) + "'");
                }
                this.msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.", new Object[0]);
                return true;
            }
            else if (args[1].equalsIgnoreCase("world")) {
                if (!plugin.hasPerm(sender, "Commands.Cleanup.world")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (db.hasTable(db.prefix + "blocks_" + args[2])) {
                    this.msg(sender, "&4There is no world called {0}", args[2]);
                }
                else {
                    db.queue("DROP TABLE `" + db.prefix + "blocks_" + args[2] + "`;");
                }
                manager.clear();
                db.load();
                this.msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.", new Object[0]);
                return true;
            }
        }
        else if (args.length > 1) {
            if (args[1].equalsIgnoreCase("all")) {
                if (!plugin.hasPerm(sender, "Commands.Cleanup.all")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                for (final World world : Bukkit.getWorlds()) {
                    db.queue("DROP TABLE `" + db.prefix + "blocks_" + world.getName() + "`");
                }
                manager.clear();
                db.load();
                manager.clear();
                this.msg(sender, "&7Command executed successfully, however, we can't tell when it will be finished.", new Object[0]);
                return true;
            }
            else if (args[1].equalsIgnoreCase("corrupt")) {
                if (!plugin.hasPerm(sender, "Commands.Cleanup.corrupt")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                CreativeSQLCleanup cleanup = null;
                if (sender instanceof Player) {
                    cleanup = new CreativeSQLCleanup(CreativeControl.plugin, (Player)sender);
                }
                else {
                    cleanup = new CreativeSQLCleanup(CreativeControl.plugin, null);
                }
                if (CreativeSQLCleanup.lock) {
                    this.msg(sender, "&4The cleanup is already running!", new Object[0]);
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)plugin, (Runnable)cleanup);
                return true;
            }
        }
        this.msg(sender, "&4/cc cleanup all &8-&7 Remove all protections", new Object[0]);
        this.msg(sender, "&4/cc cleanup corrupt &8-&7 Remove all corrupt protections", new Object[0]);
        this.msg(sender, "&4/cc cleanup type <typeId> &8-&7 Remove all protections of a type", new Object[0]);
        this.msg(sender, "&4/cc cleanup player <player> &8-&7 Remove all protections of a player", new Object[0]);
        this.msg(sender, "&4/cc cleanup world <world> &8-&7 Remove all protections of a world", new Object[0]);
        return true;
    }
    
    public boolean delCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlocksSelection selection = CreativeControl.getSelector();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return true;
        }
        if (args.length > 2) {
            if (args[1].equalsIgnoreCase("type")) {
                if (!plugin.hasPerm(sender, "Commands.Del.type")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.DELTYPE);
                return true;
            }
            else if (args[1].equalsIgnoreCase("player")) {
                if (!plugin.hasPerm(sender, "Commands.Del.player")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                selection.allBlocks(sender, args[2], CreativeBlocksSelection.Type.DELPLAYER);
                return true;
            }
        }
        else if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            if (!plugin.hasPerm(sender, "Commands.Del.all")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            selection.allBlocks(sender, sender.getName(), CreativeBlocksSelection.Type.DELALL);
            return true;
        }
        return true;
    }
    
    public boolean selCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return false;
        }
        if (config.selection_usewe) {
            this.msg(sender, "&4You must use the worldedit //expand command!", new Object[0]);
            return true;
        }
        final Player p = (Player)sender;
        Label_0458: {
            if (args.length > 3) {
                if (!args[1].equalsIgnoreCase("expand")) {
                    break Label_0458;
                }
                if (!plugin.hasPerm(sender, "Commands.Expand")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (args[2].equalsIgnoreCase("up")) {
                    try {
                        final int add = Integer.parseInt(args[3]);
                        final Location up = plugin.right.get(p);
                        if (add + up.getY() > 255.0) {
                            up.setY(255.0);
                        }
                        else {
                            up.add(0.0, (double)add, 0.0);
                        }
                        plugin.right.put(p, up);
                        this.msg(sender, "&7Selection expanded successfuly!", new Object[0]);
                        return true;
                    }
                    catch (Exception e) {
                        this.msg(sender, "&4{0} is not a valid number!", args[3]);
                        return true;
                    }
                }
                if (!args[2].equalsIgnoreCase("down")) {
                    break Label_0458;
                }
                try {
                    final int add = Integer.parseInt(args[3]);
                    final Location down = plugin.left.get(p);
                    if (add - down.getY() < 0.0) {
                        down.setY(0.0);
                    }
                    else {
                        down.subtract(0.0, (double)add, 0.0);
                    }
                    plugin.left.put(p, down);
                    this.msg(sender, "&7Selection expanded successfuly!", new Object[0]);
                    return true;
                }
                catch (Exception e) {
                    this.msg(sender, "&4{0} is not a valid number!", args[3]);
                    return true;
                }
            }
            if (args.length > 2 && args[2].equalsIgnoreCase("vert")) {
                final Location right = plugin.right.get(p);
                final Location left = plugin.right.get(p);
                right.setY(255.0);
                left.setY(0.0);
                plugin.right.put(p, right);
                plugin.left.put(p, left);
                this.msg(sender, "&7Selection expanded successfuly!", new Object[0]);
                return true;
            }
        }
        this.msg(sender, "&4/cc sel expand vert &8-&7 Expand the selection from sky to bedrock", new Object[0]);
        this.msg(sender, "&4/cc sel expand up <amount> &8-&7 Expand the selecion X blocks up", new Object[0]);
        this.msg(sender, "&4/cc sel expand down <amount> &8-&7 Expand the selection Y block down", new Object[0]);
        return true;
    }
    
    public boolean regionCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlocksSelection selection = CreativeControl.getSelector();
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return false;
        }
        final Player p = (Player)sender;
        final Location left = plugin.left.get(p);
        final Location right = plugin.right.get(p);
        if (args.length > 3) {
            if (args[1].equalsIgnoreCase("define")) {
                if (!plugin.hasPerm(sender, "Commands.Region.define")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                Location start = null;
                Location end = null;
                if (!main.selection_usewe || selection.getSelection(p) == null) {
                    if (left == null || right == null) {
                        this.msg(sender, "&4You must select the area first!", new Object[0]);
                        return true;
                    }
                    final CreativeSelection sel = new CreativeSelection(left, right);
                    start = sel.getStart();
                    end = sel.getEnd();
                }
                else {
                    final Selection sel2 = selection.getSelection((Player)sender);
                    if (sel2 == null) {
                        this.msg(sender, "&4You must select the area first!", new Object[0]);
                        return true;
                    }
                    start = sel2.getMinimumPoint();
                    end = sel2.getMaximumPoint();
                }
                GameMode type = null;
                if (args[2].equalsIgnoreCase("creative")) {
                    type = GameMode.CREATIVE;
                }
                else if (args[2].equalsIgnoreCase("adventure")) {
                    type = GameMode.ADVENTURE;
                }
                else if (args[2].equalsIgnoreCase("survival")) {
                    type = GameMode.SURVIVAL;
                }
                if (type != null) {
                    this.setRegion(type, args[3], start, end);
                    this.msg(sender, "&4{0} &7region created successfully!", type.toString().toLowerCase());
                    return true;
                }
                this.msg(sender, "&4{0} is not a valid gamemode!", args[2]);
                return true;
            }
        }
        else if (args.length > 2 && args[1].equalsIgnoreCase("remove")) {
            if (!plugin.hasPerm(sender, "Commands.Region.remove")) {
                this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                return true;
            }
            this.removeRegion(args[2]);
            this.msg(sender, "&7Region removed successfully", new Object[0]);
            return true;
        }
        this.msg(sender, "&4/cc region define creative <name> &8-&7 Create a creative region", new Object[0]);
        this.msg(sender, "&4/cc region define survival <name> &8-&7 Create a survival region", new Object[0]);
        this.msg(sender, "&4/cc region remove <name> &8-&7 Remove a region", new Object[0]);
        return true;
    }
    
    public boolean reloadCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (!plugin.hasPerm(sender, "Commands.Reload")) {
            this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
            return true;
        }
        this.msg(sender, "&7Reloading...", new Object[0]);
        plugin.reload(sender);
        this.msg(sender, "&7Reloaded successfuly!", new Object[0]);
        return true;
    }
    
    public boolean statusCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeSQLDatabase db = CreativeControl.getDb();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlockManager manager = CreativeControl.getManager();
        if (!plugin.hasPerm(sender, "Commands.Status")) {
            this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
            return true;
        }
        this.msg(sender, "&4Queue size&8:&7 {0}", db.getQueueSize());
        this.msg(sender, "&4Database reads&8:&7 {0}", db.getReads());
        this.msg(sender, "&4Database writes&8:&7 {0}", db.getWrites());
        this.msg(sender, "&4Database size&8:&7 {0} / {1}", Utils.getFormatedBytes(manager.getTablesSize()), Utils.getFormatedBytes(manager.getTablesFree()));
        try {
            this.msg(sender, "&4Database type&8:&7 {0}, &4ping&8:&7 {1} ms", db.getDatabaseEngine(), (db.ping() > 0L) ? db.ping() : "<1");
        }
        catch (CoreException ex) {}
        this.msg(sender, "&4Blocks protected&8:&7 {0}", manager.getTotal());
        this.msg(sender, "&4Cache reads&8:&7 {0}", manager.getCache().getReads());
        this.msg(sender, "&4Queue writes&8:&7 {0}", manager.getCache().getWrites());
        this.msg(sender, "&4Cache size&8:&7 {0}/{1}", manager.getCache().size(), manager.getCache().getMaxSize());
        return true;
    }
    
    public boolean toolCmd(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (!(sender instanceof Player)) {
            this.msg(sender, "&4This command can't be used here!", new Object[0]);
            return false;
        }
        final Player p = (Player)sender;
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("add")) {
                if (!plugin.hasPerm((CommandSender)p, "Commands.Tool.add")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (plugin.mods.containsKey(p.getName())) {
                    this.msg(sender, "&7Tool deactivated!", new Object[0]);
                    plugin.mods.remove(p.getName());
                    return true;
                }
                plugin.mods.put(p.getName(), 0);
                this.msg(sender, "&7Touch the block to protect", new Object[0]);
                return true;
            }
            else if (args[1].equalsIgnoreCase("del")) {
                if (!plugin.hasPerm((CommandSender)p, "Commands.Tool.del")) {
                    this.msg(sender, "&4You dont have permission to use this command!", new Object[0]);
                    return true;
                }
                if (plugin.mods.containsKey(p.getName())) {
                    this.msg(sender, "&7Tool deactivated!", new Object[0]);
                    plugin.mods.remove(p.getName());
                    return true;
                }
                plugin.mods.put(p.getName(), 1);
                this.msg(sender, "&7Touch the block to unprotect", new Object[0]);
                return true;
            }
        }
        this.msg(sender, "&4/cc tool add &8-&7 Manualy protect blocks", new Object[0]);
        this.msg(sender, "&4/cc tool del &8-&7 Manualy unprotect blocks", new Object[0]);
        return true;
    }
    
    public void setRegion(final GameMode type, final String name, final Location start, final Location end) {
        final CreativeRegionManager region = CreativeControl.getRegioner();
        region.addRegion(name, start, end, type.toString());
        region.saveRegion(name, type, start, end);
    }
    
    private void removeRegion(final String string) {
        CreativeControl.getRegioner().deleteRegion(string);
    }
    
    public void msg(final CommandSender sender, final String s, final Object... objects) {
        CreativeControl.plugin.getCommunicator().msg(sender, s, objects);
    }
}
