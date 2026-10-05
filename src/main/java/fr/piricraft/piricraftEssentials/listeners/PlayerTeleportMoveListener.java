package fr.piricraft.piricraftEssentials.listeners;

import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerTeleportMoveListener implements Listener {

    private final TeleportManager teleportManager;
    private final EssentialsDatabaseManager dbManager;

    public PlayerTeleportMoveListener(TeleportManager teleportManager, EssentialsDatabaseManager dbManager) {
        this.teleportManager = teleportManager;
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to != null && (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())) {
            if (teleportManager.hasPendingTeleport(event.getPlayer().getUniqueId())) {
                teleportManager.cancelPendingTeleport(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!event.isCancelled() && event.getFinalDamage() > 0 && event.getEntity() instanceof Player player) {
            EssentialsProfile profile = dbManager.getProfileFromCache(player.getUniqueId());
            if (profile != null && profile.isGodMode()) event.setCancelled(true);
            if (teleportManager.hasPendingTeleport(player.getUniqueId())) {
                teleportManager.cancelPendingTeleport(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EssentialsProfile profile = dbManager.getProfileFromCache(player.getUniqueId());
        if (profile != null) {
            profile.setLastLocation(player.getLocation());
        }
    }
}
