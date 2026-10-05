package fr.piricraft.piricraftEssentials.managers;

import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final PiricraftEssentials plugin;
    private final EssentialsDatabaseManager dbManager;
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();

    public TeleportManager(PiricraftEssentials plugin, EssentialsDatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    public void teleportWithWarmup(Player player, Location target, int seconds, Runnable onSuccess) {
        if (target == null || target.getWorld() == null || !player.isOnline()) return;
        UUID uuid = player.getUniqueId();

        cancelPendingTeleport(uuid);

        if (seconds <= 0) {
            executeTeleport(player, target, onSuccess);
            return;
        }

        player.sendMessage(TextUtils.color("<yellow>[Téléportation]</yellow> Téléportation dans <gold>" + seconds + "</gold> secondes. Ne bougez pas !"));

        BukkitTask task = new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelPendingTeleport(uuid);
                    return;
                }

                if (remaining <= 0) {
                    pendingTeleports.remove(uuid);
                    this.cancel();
                    executeTeleport(player, target, onSuccess);
                    return;
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        pendingTeleports.put(uuid, task);
    }

    public void cancelPendingTeleport(UUID playerUuid) {
        BukkitTask task = pendingTeleports.remove(playerUuid);
        if (task != null) {
            task.cancel();
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(TextUtils.color("<red>[Téléportation]</red> Téléportation annulée (vous avez bougé ou subi des dégâts)."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    public boolean hasPendingTeleport(UUID playerUuid) {
        return pendingTeleports.containsKey(playerUuid);
    }

    public void sendTpaRequest(Player sender, Player target) {
        EssentialsProfile targetProfile = dbManager.getProfileFromCache(target.getUniqueId());
        if (targetProfile == null) {
            sender.sendMessage(TextUtils.color("<red>[Téléportation]</red> Impossible d'envoyer la demande pour le moment."));
            return;
        }

        long expireAt = System.currentTimeMillis() + (60 * 1000L);
        targetProfile.addTpaRequest(sender.getUniqueId(), expireAt);

        sender.sendMessage(TextUtils.color("<green>[Téléportation]</green> Demande de téléportation envoyée à <gold>" + target.getName() + "</gold> (expire dans 60s)."));
        target.sendMessage(TextUtils.color("<gold>" + sender.getName() + "</gold> <yellow>souhaite se téléporter à vous.</yellow>"));
        target.sendMessage(TextUtils.color("<gray>Tapez <green>/tpaccept</green> pour accepter ou <red>/tpdeny</red> pour refuser.</gray>"));
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    private void executeTeleport(Player player, Location target, Runnable onSuccess) {
        Location previousLocation = player.getLocation().clone();
        player.teleportAsync(target).thenAccept(success -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (success) {
                EssentialsProfile profile = dbManager.getProfileFromCache(player.getUniqueId());
                if (profile != null) profile.setLastLocation(previousLocation);
                player.playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                player.sendMessage(TextUtils.color("<red>[Téléportation]</red> Échec de la téléportation."));
            }
        }));
    }
}
