package fr.piricraft.piricraftEssentials.commands;

import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffUtilityCommands implements CommandExecutor {
    private final EssentialsDatabaseManager db;
    private final PiricraftEssentials plugin;
    public StaffUtilityCommands(PiricraftEssentials plugin, EssentialsDatabaseManager db) { this.plugin = plugin; this.db = db; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = command.getName().toLowerCase();
        if (!(sender instanceof Player actor)) { sender.sendMessage("Cette commande est réservée aux joueurs."); return true; }
        String permission = "piricraft.essentials." + action;
        if (!actor.hasPermission(permission) && !actor.hasPermission("piricraft.admin")) { actor.sendMessage(TextUtils.color("<red>Permission insuffisante.")); return true; }
        Player target = args.length > 0 ? Bukkit.getPlayerExact(args[0]) : actor;
        if (target == null) { actor.sendMessage(TextUtils.color("<red>Joueur introuvable.")); return true; }
        EssentialsProfile profile = db.getProfileFromCache(target.getUniqueId());
        switch (action) {
            case "god" -> {
                if (profile == null) return true;
                profile.setGodMode(!profile.isGodMode());
                actor.sendMessage(TextUtils.color("<green>God mode " + (profile.isGodMode() ? "activé" : "désactivé") + " pour " + target.getName() + "."));
            }
            case "fly" -> {
                target.setAllowFlight(!target.getAllowFlight());
                if (!target.getAllowFlight()) target.setFlying(false);
                actor.sendMessage(TextUtils.color("<green>Vol " + (target.getAllowFlight() ? "activé" : "désactivé") + " pour " + target.getName() + "."));
            }
            case "heal" -> {
                var health = target.getAttribute(Attribute.MAX_HEALTH);
                if (health != null) target.setHealth(health.getValue());
                target.setFireTicks(0);
                actor.sendMessage(TextUtils.color("<green>" + target.getName() + " a été soigné."));
            }
            case "feed" -> {
                target.setFoodLevel(20);
                target.setSaturation(20);
                actor.sendMessage(TextUtils.color("<green>" + target.getName() + " a été nourri."));
            }
            case "vanish" -> {
                if (profile == null) return true;
                profile.setVanished(!profile.isVanished());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (profile.isVanished() && !online.hasPermission("piricraft.essentials.vanish.see")) online.hidePlayer(plugin, target);
                    else online.showPlayer(plugin, target);
                }
                actor.sendMessage(TextUtils.color("<green>Vanish " + (profile.isVanished() ? "activé" : "désactivé") + " pour " + target.getName() + "."));
            }
            case "tpthere" -> {
                if (!(sender instanceof Player)) return true;
                target.teleportAsync(actor.getLocation());
            }
        }
        return true;
    }
}
