package fr.piricraft.piricraftEssentials.listeners;

import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final PiricraftEssentials plugin;
    private final EssentialsDatabaseManager dbManager;

    public PlayerConnectionListener(PiricraftEssentials plugin, EssentialsDatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        dbManager.loadProfileAsync(event.getUniqueId()).join();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EssentialsProfile profile = dbManager.getProfileFromCache(player.getUniqueId());

        if (profile != null && profile.isVanished()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("piricraft.essentials.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            EssentialsProfile onlineProfile = dbManager.getProfileFromCache(onlinePlayer.getUniqueId());
            if (onlineProfile != null && onlineProfile.isVanished()) {
                if (!player.hasPermission("piricraft.essentials.vanish.see")) {
                    player.hidePlayer(plugin, onlinePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dbManager.unloadProfile(event.getPlayer().getUniqueId());
    }
}