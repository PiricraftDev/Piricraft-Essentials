package fr.piricraft.piricraftEssentials;

import fr.piricraft.piricraftEssentials.commands.HomeCommands;
import fr.piricraft.piricraftEssentials.commands.StaffUtilityCommands;
import fr.piricraft.piricraftEssentials.commands.TeleportCommands;
import fr.piricraft.piricraftEssentials.commands.WarpCommands;
import fr.piricraft.piricraftEssentials.listeners.PlayerConnectionListener;
import fr.piricraft.piricraftEssentials.listeners.PlayerTeleportMoveListener;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class PiricraftEssentials extends JavaPlugin {

    private EssentialsDatabaseManager databaseManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new EssentialsDatabaseManager(this);
        this.databaseManager.initDatabase();
        this.databaseManager.loadWarps();

        this.teleportManager = new TeleportManager(this, databaseManager);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this, databaseManager), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportMoveListener(teleportManager, databaseManager), this);

        registerCommands();
    }

    @Override
    public void onDisable() {
        if (databaseManager == null) return;
        CompletableFuture<?>[] saves = databaseManager.getLoadedProfiles().stream()
                .map(databaseManager::saveProfileAsync)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(saves).join();
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportManager.cancelPendingTeleport(player.getUniqueId());
        }
        databaseManager.closeConnection();
    }

    private void registerCommands() {
        HomeCommands homeCommands = new HomeCommands(this, databaseManager, teleportManager);
        getCommand("home").setExecutor(homeCommands);
        getCommand("sethome").setExecutor(homeCommands);
        getCommand("delhome").setExecutor(homeCommands);
        getCommand("homes").setExecutor(homeCommands);

        WarpCommands warpCommands = new WarpCommands(this, databaseManager, teleportManager);
        getCommand("warp").setExecutor(warpCommands);
        getCommand("setwarp").setExecutor(warpCommands);
        getCommand("delwarp").setExecutor(warpCommands);
        getCommand("warps").setExecutor(warpCommands);

        TeleportCommands teleportCommands = new TeleportCommands(this, databaseManager, teleportManager);
        getCommand("tp").setExecutor(teleportCommands);
        getCommand("tpa").setExecutor(teleportCommands);
        getCommand("tpaccept").setExecutor(teleportCommands);
        getCommand("tpdeny").setExecutor(teleportCommands);
        getCommand("back").setExecutor(teleportCommands);
        getCommand("spawn").setExecutor(teleportCommands);

        StaffUtilityCommands staffCommands = new StaffUtilityCommands(this, databaseManager);
        getCommand("god").setExecutor(staffCommands);
        getCommand("fly").setExecutor(staffCommands);
        getCommand("heal").setExecutor(staffCommands);
        getCommand("feed").setExecutor(staffCommands);
        getCommand("vanish").setExecutor(staffCommands);
        getCommand("tpthere").setExecutor(staffCommands);
    }

    public EssentialsDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
}
