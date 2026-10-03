package fr.piricraft.piricraftEssentials;

import fr.piricraft.piricraftEssentials.listeners.PlayerConnectionListener;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

        getLogger().info("PiricraftEssentials a ete active avec succes !");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            for (var player : Bukkit.getOnlinePlayers()) {
                databaseManager.unloadProfile(player.getUniqueId());
            }
            databaseManager.closeConnection();
        }

        getLogger().info("PiricraftEssentials a ete desactive !");
    }

    public EssentialsDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
}