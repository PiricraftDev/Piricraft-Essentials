package fr.piricraft.piricraftEssentials.commands;

import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.gui.WarpsMenu;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.UserWarp;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class WarpCommands implements CommandExecutor {
    private final PiricraftEssentials plugin;
    private final EssentialsDatabaseManager db;
    private final TeleportManager teleports;

    public WarpCommands(PiricraftEssentials plugin, EssentialsDatabaseManager db, TeleportManager teleports) {
        this.plugin = plugin;
        this.db = db;
        this.teleports = teleports;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = command.getName().toLowerCase(Locale.ROOT);
        if (action.equals("warps")) {
            if (sender instanceof Player player) new WarpsMenu(db, teleports, plugin.getConfig().getInt("teleportation.warmup-seconds", 3)).open(player);
            else sender.sendMessage("Cette commande est réservée aux joueurs.");
            return true;
        }
        if (action.equals("warp")) {
            if (!(sender instanceof Player player)) return true;
            if (args.length == 0) { player.sendMessage(TextUtils.color("<red>Usage: /warp <nom></red>")); return true; }
            UserWarp warp = db.getWarp(args[0]);
            if (warp == null) { player.sendMessage(TextUtils.color("<red>Ce warp n'existe pas.")); return true; }
            if (warp.getPermission() != null && !player.hasPermission(warp.getPermission())) { player.sendMessage(TextUtils.color("<red>Vous n'avez pas accès à ce warp.")); return true; }
            teleports.teleportWithWarmup(player, warp.getLocation(), plugin.getConfig().getInt("teleportation.warmup-seconds", 3), null);
            return true;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage("Cette commande est réservée aux joueurs."); return true; }
        if (args.length == 0) { player.sendMessage(TextUtils.color("<red>Usage: /" + action + " <nom></red>")); return true; }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("setwarp")) {
            if (!name.matches("[a-z0-9_-]{1,32}")) { player.sendMessage(TextUtils.color("<red>Nom invalide. Utilisez 1 à 32 caractères: lettres, chiffres, _ ou -.</red>")); return true; }
            UserWarp existing = db.getWarp(name);
            boolean admin = player.hasPermission("piricraft.admin") || player.hasPermission("piricraft.warps.unlimited");
            if (existing != null && !existing.getOwnerUuid().equals(player.getUniqueId()) && !admin) { player.sendMessage(TextUtils.color("<red>Ce warp appartient à un autre joueur.")); return true; }
            int limit = plugin.getConfig().getInt("warps.default-limit", 1);
            for (int tier : new int[]{1, 2, 3, 5, 10}) if (player.hasPermission("piricraft.warps.limit." + tier)) limit = Math.max(limit, tier);
            if (existing == null && !admin && db.getWarpCountByPlayer(player.getUniqueId()) >= limit) { player.sendMessage(TextUtils.color("<red>Vous avez atteint votre limite de warps.")); return true; }
            db.addWarp(new UserWarp(name, player.getUniqueId(), player.getLocation(), Material.ENDER_PEARL, null));
            player.sendMessage(TextUtils.color("<green>Warp <gold>" + name + "</gold> enregistré."));
        } else {
            UserWarp warp = db.getWarp(name);
            if (warp == null) { player.sendMessage(TextUtils.color("<red>Ce warp n'existe pas.")); return true; }
            if (!warp.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("piricraft.admin") && !player.hasPermission("piricraft.warps.unlimited")) { player.sendMessage(TextUtils.color("<red>Vous ne pouvez pas supprimer ce warp.")); return true; }
            db.removeWarp(name);
            player.sendMessage(TextUtils.color("<green>Warp <gold>" + name + "</gold> supprimé."));
        }
        return true;
    }
}
