package fr.piricraft.piricraftEssentials.commands;

import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import fr.piricraft.piricraftEssentials.models.UserHome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Locale;

public class HomeCommands implements CommandExecutor {

    private final PiricraftEssentials plugin;
    private final EssentialsDatabaseManager dbManager;
    private final TeleportManager teleportManager;

    public HomeCommands(PiricraftEssentials plugin, EssentialsDatabaseManager dbManager, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Cette commande est réservée aux joueurs."); return true; }

        EssentialsProfile profile = dbManager.getProfileFromCache(player.getUniqueId());
        if (profile == null) { player.sendMessage(TextUtils.color("<red>Profil en cours de chargement, réessayez.")); return true; }

        switch (label.toLowerCase()) {
            case "sethome" -> {
                String homeName = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "home";
                if (!homeName.matches("[a-z0-9_-]{1,32}")) { player.sendMessage(TextUtils.color("<red>Nom invalide. Utilisez 1 à 32 caractères: lettres, chiffres, _ ou -.</red>")); return true; }
                int maxHomes = plugin.getConfig().getInt("homes.default-limit", 2);
                var limits = plugin.getConfig().getConfigurationSection("homes.limits");
                if (limits != null) for (String key : limits.getKeys(false)) { int limit = limits.getInt(key); if (player.hasPermission("piricraft.homes.limit." + limit) || player.hasPermission("piricraft.homes.limit." + key)) maxHomes = Math.max(maxHomes, limit); }

                if (profile.getHomes().size() >= maxHomes && !profile.getHomes().containsKey(homeName) && !player.hasPermission("piricraft.homes.unlimited")) {
                    player.sendMessage(TextUtils.color("<red>[Homes]</red> Vous avez atteint votre limite de homes (<gold>" + maxHomes + "</gold>)."));
                    return true;
                }

                profile.addHome(new UserHome(homeName, player.getLocation(), System.currentTimeMillis()));
                dbManager.saveProfileAsync(profile);
                player.sendMessage(TextUtils.color("<green>[Homes]</green> Home <gold>" + homeName + "</gold> crÃ©Ã© avec succÃ¨s !"));
            }
            case "delhome" -> {
                if (args.length == 0) {
                    player.sendMessage(TextUtils.color("<red>Usage: /delhome <nom></red>"));
                    return true;
                }
                String homeName = args[0].toLowerCase(Locale.ROOT);
                if (!profile.getHomes().containsKey(homeName)) {
                    player.sendMessage(TextUtils.color("<red>[Homes]</red> Le home <gold>" + homeName + "</gold> n'existe pas."));
                    return true;
                }
                profile.removeHome(homeName);
                dbManager.saveProfileAsync(profile);
                player.sendMessage(TextUtils.color("<green>[Homes]</green> Home <gold>" + homeName + "</gold> supprimÃ©."));
            }
            case "home" -> {
                String homeName = args.length > 0 ? args[0].toLowerCase() : "home";
                UserHome home = profile.getHomes().get(homeName);
                if (home == null) {
                    player.sendMessage(TextUtils.color("<red>[Homes]</red> Aucun home trouvÃ© sous le nom <gold>" + homeName + "</gold>."));
                    return true;
                }
                teleportManager.teleportWithWarmup(player, home.getLocation(), plugin.getConfig().getInt("teleportation.warmup-seconds", 3), () -> {
                    player.sendMessage(TextUtils.color("<green>[Homes]</green> TÃ©lÃ©portÃ© au home <gold>" + home.getName() + "</gold>."));
                });
            }
            case "homes" -> new fr.piricraft.piricraftEssentials.gui.HomesMenu(profile, teleportManager, plugin.getConfig().getInt("teleportation.warmup-seconds", 3)).open(player);
        }

        return true;
    }
}
