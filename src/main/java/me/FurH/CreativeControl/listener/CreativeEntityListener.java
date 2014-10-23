package me.FurH.CreativeControl.listener;

import me.FurH.CreativeControl.*;
import org.bukkit.command.*;
import me.FurH.CreativeControl.core.util.*;
import me.FurH.CreativeControl.configuration.*;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.block.*;
import me.FurH.CreativeControl.manager.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;

public class CreativeEntityListener implements Listener
{
    public static List<Player> waiting;
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onVehicleCreate(final VehicleCreateEvent e) {
        final CreativeControl plugin = CreativeControl.getPlugin();
        final Vehicle vehicle = e.getVehicle();
        if (CreativeEntityListener.waiting.isEmpty()) {
            return;
        }
        final Player p = CreativeEntityListener.waiting.remove(0);
        if (p == null) {
            return;
        }
        final Communicator com = plugin.getCommunicator();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(vehicle.getWorld());
        if (config.world_exclude) {
            return;
        }
        if (config.prevent_vehicle && !plugin.hasPerm((CommandSender)p, "Preventions.Vehicle")) {
            final int limit = config.prevent_limitvechile;
            HashSet<UUID> entities = new HashSet<UUID>();
            if (plugin.limits.containsKey(p.getName())) {
                entities = plugin.limits.get(p.getName());
            }
            final int total = entities.size();
            if (limit > 0 && total >= limit) {
                com.msg((CommandSender)p, messages.limits_vehicles, new Object[0]);
                vehicle.remove();
            }
            else {
                entities.add(vehicle.getUniqueId());
                plugin.limits.put(p.getName(), entities);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Entity entity = e.getAttacker();
        final Vehicle vehicle = e.getVehicle();
        if (!(entity instanceof Player)) {
            return;
        }
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(vehicle.getWorld());
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (config.prevent_vehicle) {
            final String master = plugin.removeVehicle(vehicle.getUniqueId());
            if (master == null) {
                return;
            }
            e.setCancelled(true);
            vehicle.remove();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityExplode(final EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getLocation().getWorld());
        if (config.world_exclude) {
            return;
        }
        final List<Block> oldList = new ArrayList<Block>();
        oldList.addAll(e.blockList());
        if (config.block_explosion) {
            final CreativeBlockManager manager = CreativeControl.getManager();
            if (e.blockList() != null && e.blockList().size() > 0) {
                for (final Block b : e.blockList()) {
                    if (b != null && b.getType() != Material.AIR) {
                        if (config.block_ownblock) {
                            if (manager.isprotected(b, true) == null) {
                                continue;
                            }
                            oldList.remove(b);
                        }
                        else {
                            if (!config.block_nodrop || manager.isprotected(b, true) == null) {
                                continue;
                            }
                            manager.unprotect(b);
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
            e.blockList().clear();
            e.blockList().addAll(oldList);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityTarget(final EntityTargetEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getTarget() instanceof Player)) {
            return;
        }
        final Player p = (Player)e.getTarget();
        final World world = p.getWorld();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final CreativeControl plugin = CreativeControl.getPlugin();
        if (config.world_exclude) {
            return;
        }
        if (config.prevent_target && p.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)p, "Preventions.Target")) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        final World world = p.getWorld();
        final Entity entity = e.getRightClicked();
        final CreativeMessages messages = CreativeControl.getMessages();
        final CreativeControl plugin = CreativeControl.getPlugin();
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
        final Communicator com = plugin.getCommunicator();
        if (config.world_exclude) {
            return;
        }
        if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
            if (entity instanceof StorageMinecart || entity instanceof PoweredMinecart) {
                if (config.prevent_mcstore && !plugin.hasPerm((CommandSender)p, "Preventions.MineCartStorage")) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                }
            }
            else if (entity instanceof Villager) {
                if (config.prevent_villager && !plugin.hasPerm((CommandSender)p, "Preventions.InteractVillagers")) {
                    com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                }
            }
            else if (entity instanceof ItemFrame && config.prevent_frame && !plugin.hasPerm((CommandSender)p, "Preventions.ItemFrame")) {
                com.msg((CommandSender)p, messages.mainode_restricted, new Object[0]);
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDeath(final EntityDeathEvent e) {
        if (e.getEntity().getKiller() instanceof Player) {
            final Player p = e.getEntity().getKiller();
            final World world = p.getWorld();
            if (p.getGameMode().equals((Object)GameMode.CREATIVE)) {
                final CreativeControl plugin = CreativeControl.getPlugin();
                final CreativeWorldNodes config = CreativeControl.getWorldNodes(world);
                if (config.world_exclude) {
                    return;
                }
                if (config.prevent_mobsdrop && !plugin.hasPerm((CommandSender)p, "Preventions.MobsDrop") && e.getEntity() instanceof Creature) {
                    e.setDroppedExp(0);
                    e.getDrops().clear();
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL && CreativePlayerListener.changed.remove(((Player)event.getEntity()).getName())) {
            event.setCancelled(true);
            event.setDamage(0.0);
        }
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
            final CreativeWorldNodes config = CreativeControl.getWorldNodes(e.getDamager().getWorld());
            final CreativeMessages messages = CreativeControl.getMessages();
            final CreativeControl plugin = CreativeControl.getPlugin();
            final Communicator com = plugin.getCommunicator();
            if (config.world_exclude) {
                return;
            }
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                final Player attacker = (Player)e.getDamager();
                final Player defender = (Player)e.getEntity();
                if (config.prevent_pvp && attacker.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)attacker, "Preventions.PvP")) {
                    com.msg((CommandSender)attacker, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                }
                if (!e.isCancelled()) {
                    this.removeFlyAndGameMode(attacker, defender);
                }
            }
            else if (e.getDamager() instanceof Player && e.getEntity() instanceof Creature) {
                final Player attacker = (Player)e.getDamager();
                if (config.prevent_mobs && attacker.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)attacker, "Preventions.Mobs")) {
                    com.msg((CommandSender)attacker, messages.mainode_restricted, new Object[0]);
                    e.setCancelled(true);
                }
            }
            else if (e.getDamager() instanceof Projectile) {
                final Projectile projectile = (Projectile)e.getDamager();
                if (projectile.getShooter() instanceof Player && e.getEntity() instanceof Player) {
                    final Player attacker2 = (Player)projectile.getShooter();
                    if (config.prevent_pvp && attacker2.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)attacker2, "Preventions.PvP")) {
                        com.msg((CommandSender)attacker2, messages.mainode_restricted, new Object[0]);
                        e.setCancelled(true);
                    }
                    if (!e.isCancelled()) {
                        this.removeFlyAndGameMode(attacker2, (Player)e.getEntity());
                    }
                }
                else if (projectile.getShooter() instanceof Player && e.getEntity() instanceof Creature) {
                    final Player attacker2 = (Player)projectile.getShooter();
                    if (config.prevent_mobs && attacker2.getGameMode().equals((Object)GameMode.CREATIVE) && !plugin.hasPerm((CommandSender)attacker2, "Preventions.Mobs")) {
                        com.msg((CommandSender)attacker2, messages.mainode_restricted, new Object[0]);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    
    private void removeFlyAndGameMode(final Player attacker, final Player defender) {
        final CreativeWorldNodes config = CreativeControl.getWorldNodes(attacker.getWorld());
        if (config.prevent_fly) {
            if (attacker.getAllowFlight()) {
                attacker.setAllowFlight(false);
            }
            if (defender.getAllowFlight()) {
                defender.setAllowFlight(false);
            }
        }
        if (config.prevent_creative) {
            if (attacker.getGameMode().equals((Object)GameMode.CREATIVE)) {
                attacker.setGameMode(GameMode.SURVIVAL);
            }
            if (defender.getGameMode().equals((Object)GameMode.CREATIVE)) {
                defender.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
    
    static {
        CreativeEntityListener.waiting = new ArrayList<Player>();
    }
}
