package me.FurH.CreativeControl.listener;

import me.FurH.CreativeControl.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.data.*;
import me.FurH.CreativeControl.region.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.configuration.*;
import me.FurH.CreativeControl.database.*;
import net.milkbowl.vault.permission.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.enchantment.*;
import org.bukkit.inventory.*;
import me.FurH.CreativeControl.stack.*;
import me.FurH.CreativeControl.blacklist.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.plugin.*;
import me.FurH.CreativeControl.core.player.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.*;
import org.bukkit.block.*;
import me.FurH.CreativeControl.util.*;
import me.FurH.CreativeControl.core.location.*;
import java.util.*;
import me.FurH.CreativeControl.manager.*;
import me.FurH.CreativeControl.core.cache.soft.*;
import me.FurH.CreativeControl.data.friend.*;

public class CreativePlayerListener implements Listener
{
    public static HashSet<String> changed;
    private HashSet<String> dontdrop;
    
    public CreativePlayerListener() {
        super();
        this.dontdrop = new HashSet<String>();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player player = e.getPlayer();
        final GameMode newgm = e.getNewGameMode();
        final GameMode oldgm = player.getGameMode();
        final CreativeMainConfig config = CreativeControl.getMainConfig();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativePlayerData data = CreativeControl.getPlayerData();
        final CreativeRegionManager manager = CreativeControl.getRegioner();
        final CreativeRegion region = manager.getRegion(player.getLocation());
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeWorldNodes wconfig = CreativeControl.getWorldConfig().get(player.getWorld());
        final CreativeSQLDatabase db = CreativeControl.getDb();
        if (config.data_glitch) {
            if (!newgm.equals((Object)GameMode.CREATIVE) && !player.isOnGround()) {
                CreativePlayerListener.changed.add(player.getName());
            }
            else {
                CreativePlayerListener.changed.remove(player.getName());
            }
        }
        if (config.data_inventory && !plugin.hasPerm((CommandSender)player, "Data.Status")) {
            final InventoryView view = player.getOpenInventory();
            view.close();
            data.process(player, newgm, oldgm);
        }
        if (config.perm_enabled && !plugin.hasPerm((CommandSender)player, "Permission.Change")) {
            final Permission permissions = CreativeControl.getPermissions2().getVault();
            if (permissions != null) {
                if (newgm.equals((Object)GameMode.CREATIVE)) {
                    if (config.perm_keep) {
                        permissions.playerAddGroup(player, config.perm_creative);
                    }
                    else {
                        final String[] groups = permissions.getPlayerGroups(player);
                        db.saveOldGroups(player, permissions.getPlayerGroups(player));
                        for (final String group : groups) {
                            permissions.playerRemoveGroup(player, group);
                        }
                        permissions.playerAddGroup(player, config.perm_creative);
                    }
                }
                else if (config.perm_keep) {
                    permissions.playerRemoveGroup(player, config.perm_creative);
                }
                else {
                    final String[] current = permissions.getPlayerGroups(player);
                    final String[] groups2 = db.getOldGroup(player);
                    Arrays.sort(groups2, Collections.reverseOrder());
                    if (groups2 != null) {
                        for (final String group2 : current) {
                            permissions.playerRemoveGroup(player, group2);
                        }
                        for (final String old : groups2) {
                            permissions.playerAddGroup(player, old);
                        }
                    }
                }
            }
            else {
                com.log("The permissions function only works if Vault is installed!", new Object[0]);
            }
        }
        if (region != null && !newgm.equals((Object)region.gamemode) && !newgm.equals((Object)wconfig.world_gamemode) && !plugin.hasPerm((CommandSender)player, "Region.Change")) {
            com.msg((CommandSender)player, messages.region_cant_change, new Object[0]);
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (config.world_exclude) {
            return;
        }
        String cmd = e.getMessage().toLowerCase();
        if (cmd.contains(" ")) {
            cmd = cmd.split(" ")[0];
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            if (config.black_cmds.isEmpty()) {
                return;
            }
            if (config.black_cmds.contains(cmd) && !plugin.hasPerm((CommandSender)p, "BlackList.Commands") && !plugin.hasPerm((CommandSender)p, "BlackList.Commands." + cmd)) {
                com.msg((CommandSender)p, messages.blacklist_commands, p.getGameMode().toString().toLowerCase());
                e.setCancelled(true);
            }
        }
        else {
            if (config.black_s_cmds.isEmpty()) {
                return;
            }
            if (config.black_s_cmds.contains(cmd) && !plugin.hasPerm((CommandSender)p, "BlackList.SurvivalCommands") && !plugin.hasPerm((CommandSender)p, "BlackList.SurvivalCommands." + cmd)) {
                com.msg((CommandSender)p, messages.blacklist_commands, p.getGameMode().toString().toLowerCase());
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.prevent_drops && !plugin.hasPerm((CommandSender)p, "Preventions.ClearDrops")) {
            e.getDrops().clear();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnchantItemEvent(final EnchantItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getEnchanter();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)p, "Preventions.Enchantments") && config.prevent_enchant) {
            e.setCancelled(true);
        }
    }
    
    public void onInventoryBlackList(final Player p, final ItemStack item, final InventoryCreativeEvent e) {
        if (p == null || item == null || e == null) {
            return;
        }
        final CreativeItemStack stack = new CreativeItemStack(item.getTypeId(), item.getData().getData());
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(p.getWorld());
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        if (config.world_exclude) {
            return;
        }
        if (!p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            return;
        }
        if (!plugin.hasPerm((CommandSender)p, "BlackList.Inventory")) {
            if (blacklist.isBlackListed(config.black_inventory, stack)) {
                e.setCancelled(true);
                return;
            }
            if (blacklist.isBlackListed(config.black_place, stack)) {
                e.setCancelled(true);
                return;
            }
            if (blacklist.isBlackListed(config.black_use, stack)) {
                e.setCancelled(true);
                return;
            }
        }
        if (!plugin.hasPerm((CommandSender)p, "Preventions.StackLimit") && config.prevent_stacklimit > 0 && config.prevent_stacklimit < item.getAmount()) {
            item.setAmount(config.prevent_stacklimit);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryOpen(final InventoryOpenEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final HumanEntity entity = e.getPlayer();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player p = (Player)entity;
        final World world = p.getWorld();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.prevent_invinteract && !plugin.hasPerm((CommandSender)p, "Preventions.InventoryOpen")) {
            com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
            p.closeInventory();
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(final InventoryCreativeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player p = (Player)e.getWhoClicked();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            if (config.prevent_invinteract && e.getInventory().getType() == InventoryType.PLAYER && e.getSlotType() != InventoryType.SlotType.QUICKBAR) {
                if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
                    if (!plugin.hasPerm((CommandSender)p, "Preventions.InventoryArmor")) {
                        e.setCancelled(true);
                        return;
                    }
                }
                else if (!plugin.hasPerm((CommandSender)p, "Preventions.InventoryInteract")) {
                    e.setCancelled(true);
                    return;
                }
            }
            this.onInventoryBlackList(p, e.getCurrentItem(), e);
            this.onInventoryBlackList(p, e.getCursor(), e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerKick(final PlayerKickEvent e) {
        this.cleanup(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        this.cleanup(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getTo().getWorld());
        if (config.world_exclude) {
            return;
        }
        processRegion(e.getPlayer(), e.getTo());
    }
    
    public static void processRegion(final Player p, final Location to) {
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeRegion region = CreativeControl.getRegioner().getRegion(to);
        if (region == null) {
            return;
        }
        if (region.gamemode != null && !plugin.hasPerm((CommandSender)p, "Region.Keep") && !p.getGameMode().equals((Object)region.gamemode)) {
            com.msg((CommandSender)p, messages.region_welcome, region.gamemode.toString().toLowerCase());
            p.setGameMode(region.gamemode);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerLogin(final PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            final String player = e.getPlayer().getName();
            this.dontdrop.add(player);
            Bukkit.getScheduler().runTaskLater((Plugin)CreativeControl.getPlugin(), (Runnable)new Runnable() {
                @Override
                public void run() {
                    CreativePlayerListener.this.dontdrop.remove(player);
                }
            }, 100L);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (CreativeControl.getMainConfig().data_teleport) {
            PlayerUtils.toSafeLocation(p);
        }
        if (CreativeControl.getMainConfig().data_survival) {
            p.setGameMode(GameMode.SURVIVAL);
        }
        if (plugin.updater.isUpdateAvailable() && plugin.hasPerm((CommandSender)p, "Updater.Broadcast")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)plugin, (Runnable)new Runnable() {
                @Override
                public void run() {
                    plugin.updater.announce(p);
                }
            }, 40L);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent e) {
        onPlayerWorldChange(e.getPlayer(), true);
    }
    
    public static boolean onPlayerWorldChange(final Player p, final boolean blocks) {
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(p.getWorld());
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (!config.world_changegm || p.getGameMode().equals((Object)config.world_gamemode)) {
            return false;
        }
        if (plugin.hasPerm((CommandSender)p, "World.Keep")) {
            return false;
        }
        if (plugin.hasPerm((CommandSender)p, "World.Keep." + p.getWorld().getName())) {
            return false;
        }
        if (CreativeControl.getMainConfig().data_teleport) {
            PlayerUtils.toSafeLocation(p);
        }
        com.msg((CommandSender)p, messages.region_unallowed, p.getGameMode().toString().toLowerCase());
        p.setGameMode(config.world_gamemode);
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerPickupItem(final PlayerPickupItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.prevent_pickup && !plugin.hasPerm((CommandSender)p, "Preventions.Pickup")) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerDropItemOhNoes(final PlayerDropItemEvent e) {
        if (this.dontdrop.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDropItem(final PlayerDropItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (config.world_exclude) {
            return;
        }
        if (this.dontdrop.contains(p.getName())) {
            e.setCancelled(true);
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.prevent_drop && !plugin.hasPerm((CommandSender)p, "Preventions.ItemDrop")) {
            com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
            e.getItemDrop().remove();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerEggThrowEvent(final PlayerEggThrowEvent e) {
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && config.prevent_eggs && !plugin.hasPerm((CommandSender)p, "Preventions.Eggs")) {
            com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
            e.setHatching(false);
            e.setNumHatches((byte)0);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Block i = e.getClickedBlock();
        final World world = p.getWorld();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeMainConfig main = CreativeControl.getMainConfig();
        final CreativeBlackList blacklist = CreativeControl.getBlackList();
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) && processEconomySign(p, i)) {
            e.setCancelled(true);
            return;
        }
        if (main.selection_tool == p.getItemInHand().getTypeId() && plugin.hasPerm((CommandSender)p, "Utily.Selection")) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                final Location right = e.getClickedBlock().getLocation();
                plugin.right.put(p, right);
                com.msg((CommandSender)p, messages.selection_second, right.getBlockX(), right.getBlockY(), right.getBlockZ());
                e.setCancelled(true);
                return;
            }
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                final Location left = e.getClickedBlock().getLocation();
                plugin.left.put(p, left);
                com.msg((CommandSender)p, messages.selection_first, left.getBlockX(), left.getBlockY(), left.getBlockZ());
                e.setCancelled(true);
                return;
            }
        }
        if (plugin.mods.containsKey(p.getName())) {
            final int id = plugin.mods.get(p.getName());
            if (id == 0) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm((CommandSender)p, "Utily.Tool.info")) {
                        this.info(p, i);
                        e.setCancelled(true);
                        return;
                    }
                }
                else if (e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.hasPerm((CommandSender)p, "Utily.Tool.add")) {
                    this.add(p, i);
                    e.setCancelled(true);
                    return;
                }
            }
            else if (id == 1) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (plugin.hasPerm((CommandSender)p, "Utily.Tool.info")) {
                        this.info(p, i);
                        e.setCancelled(true);
                        return;
                    }
                }
                else if (e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.hasPerm((CommandSender)p, "Utily.Tool.del")) {
                    this.del(p, i);
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (config.world_exclude) {
            return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            final CreativeItemStack itemStack = new CreativeItemStack(i.getTypeId(), i.getData());
            if (blacklist.isBlackListed(config.black_interact, itemStack) && !plugin.hasPerm((CommandSender)p, "BlackList.ItemInteract." + i.getTypeId())) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                e.setCancelled(true);
                return;
            }
        }
        if ((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getMaterial() == Material.MINECART || e.getMaterial() == Material.BOAT) && p.getGameMode().equals((Object)GameMode.CREATIVE) && !CreativeEntityListener.waiting.contains(p)) {
            CreativeEntityListener.waiting.add(p);
        }
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            if (e.getItem() != null) {
                final CreativeItemStack itemStack = new CreativeItemStack(e.getItem().getTypeId(), e.getItem().getData().getData());
                if (blacklist.isBlackListed(config.black_use, itemStack) && !plugin.hasPerm((CommandSender)p, "BlackList.ItemUse." + e.getItem().getTypeId())) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                    return;
                }
            }
            if (p.getItemInHand() != null) {
                final CreativeItemStack itemStack = new CreativeItemStack(p.getItemInHand().getTypeId(), p.getItemInHand().getData().getData());
                if (blacklist.isBlackListed(config.black_use, itemStack) && !plugin.hasPerm((CommandSender)p, "BlackList.ItemUse." + p.getItemInHand().getTypeId())) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                    return;
                }
                if (config.prevent_eggs && (p.getItemInHand().getType() == Material.MONSTER_EGG || p.getItemInHand().getType() == Material.MONSTER_EGGS) && !plugin.hasPerm((CommandSender)p, "Preventions.Eggs")) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                    return;
                }
                if (config.prevent_potion && p.getItemInHand().getTypeId() == 373 && !plugin.hasPerm((CommandSender)p, "Preventions.PotionSplash")) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                }
            }
        }
    }
    
    public static boolean processEconomySign(final Player p, final Block block) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        if (p.getGameMode().equals((Object)GameMode.CREATIVE) && block != null && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
            final Sign sign = (Sign)block.getState();
            if (CreativeUtil.isBlackListedSign(sign) && !plugin.hasPerm((CommandSender)p, "BlackList.EconomySigns")) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                return true;
            }
        }
        return false;
    }
    
    public void info(final Player p, final Block b) {
        if (!this.is(p, b)) {
            return;
        }
        final CreativeWorldNodes nodes = CreativeControl.getWorldNodes(b.getWorld());
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CoreSoftCache<String, CreativeBlockData> cache = manager.getCache();
        final CreativeBlockData data1 = manager.getFullData(b.getLocation());
        CreativeBlockData data2 = null;
        if (nodes.block_ownblock) {
            data2 = cache.get(LocationUtils.locationToString(b.getLocation()));
        }
        final boolean insql = data1 != null;
        boolean incache = data2 != null;
        if (nodes.block_nodrop) {
            incache = cache.containsKey(LocationUtils.locationToString(b.getLocation()));
        }
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Communicator com = plugin.getCommunicator();
        if (!insql && !incache) {
            com.msg((CommandSender)p, messages.blockmanager_unprotected, new Object[0]);
            plugin.mods.remove(p.getName());
            return;
        }
        String owner = null;
        String allowed = null;
        int type = 0;
        String date = null;
        if (insql) {
            owner = data1.owner;
            allowed = new ArrayList(data1.allowed).toString();
            type = data1.type;
            date = data1.date;
        }
        if (incache) {
            if (data2.owner != null) {
                owner = data2.owner;
            }
            if (data2.allowed != null) {
                allowed = new ArrayList(data2.allowed).toString();
            }
            type = data2.type;
            date = Long.toString(System.currentTimeMillis());
        }
        final Location loc = b.getLocation();
        com.msg((CommandSender)p, "&4Owner&8:&7 {0}", owner);
        com.msg((CommandSender)p, "&4Data&8:&7 W&8: &4{0}&7 X&8:&4{1}&7 Y&8:&4{2}&7 Z&8:&4{3}&7 T&8:&4{4}&8/&4{5}", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getBlock().getTypeId(), type);
        if (!"".equals(allowed) && allowed != null && !"null".equals(allowed) && !allowed.isEmpty() && !"[]".equals(allowed)) {
            com.msg((CommandSender)p, "&7Permissions&8: &4{0}", allowed.replaceAll(" ,", " &a,&7").replaceAll("\\[", "").replaceAll("\\]", ""));
        }
        com.msg((CommandSender)p, "&7Status: &4{0}&7 &4{1}", incache ? "&4In Cache &7e" : "", insql ? "&4SQL Database" : "&4On Queue&8 [Memory]");
        com.msg((CommandSender)p, "&7Created in: &4{0}", CreativeUtil.getDate(Long.parseLong(date)));
        plugin.mods.remove(p.getName());
    }
    
    public void add(final Player p, final Block b) {
        if (!this.is(p, b)) {
            return;
        }
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        final Communicator com = plugin.getCommunicator();
        if (config.block_ownblock) {
            final CreativeBlockData data = manager.isprotected(b, true);
            if (data != null) {
                com.msg((CommandSender)p, messages.blockmanager_belongs, data.owner);
            }
            else {
                com.msg((CommandSender)p, messages.blockmanager_protected, new Object[0]);
                manager.protect(p, b);
            }
        }
        else if (config.block_nodrop) {
            if (manager.isprotected(b, false) != null) {
                com.msg((CommandSender)p, messages.blockmanager_already, new Object[0]);
            }
            else {
                com.msg((CommandSender)p, messages.blockmanager_protected, new Object[0]);
                manager.protect(p, b);
            }
        }
        plugin.mods.remove(p.getName());
    }
    
    public void del(final Player p, final Block b) {
        if (!this.is(p, b)) {
            return;
        }
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        final Communicator com = plugin.getCommunicator();
        if (config.block_ownblock) {
            final CreativeBlockData data = manager.isprotected(b, true);
            if (data != null) {
                if (!data.owner.equalsIgnoreCase(p.getName())) {
                    com.msg((CommandSender)p, messages.blockmanager_belongs, data.owner);
                }
                else {
                    com.msg((CommandSender)p, messages.blockmanager_removed, new Object[0]);
                    manager.unprotect(b);
                }
            }
            else {
                com.msg((CommandSender)p, messages.blockmanager_unprotected, new Object[0]);
            }
        }
        else if (config.block_nodrop) {
            if (manager.isprotected(b, true) != null) {
                com.msg((CommandSender)p, messages.blockmanager_unprotected, new Object[0]);
            }
            else {
                com.msg((CommandSender)p, messages.blockmanager_removed, new Object[0]);
                manager.unprotect(b);
            }
        }
        plugin.mods.remove(p.getName());
    }
    
    private boolean is(final Player p, final Block b) {
        final CreativeBlockManager manager = CreativeControl.getManager();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(b.getWorld());
        final Communicator com = plugin.getCommunicator();
        if (config.world_exclude) {
            com.msg((CommandSender)p, messages.blockmanager_worldexcluded, new Object[0]);
            plugin.mods.remove(p.getName());
            return false;
        }
        if (!manager.isprotectable(b.getWorld(), b.getTypeId())) {
            com.msg((CommandSender)p, messages.blockmanager_excluded, new Object[0]);
            plugin.mods.remove(p.getName());
            return false;
        }
        return true;
    }
    
    private void cleanup(final Player p) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        plugin.clear(p);
        plugin.right.remove(p);
        plugin.left.remove(p);
        plugin.mods.remove(p.getName());
        final CreativePlayerFriends friend = CreativeControl.getFriends();
        friend.uncache(p);
        final CreativePlayerData data = CreativeControl.getPlayerData();
        data.clear(p.getName());
    }
    
    static {
        CreativePlayerListener.changed = new HashSet<String>();
    }
}
