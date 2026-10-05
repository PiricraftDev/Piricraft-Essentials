package fr.piricraft.piricraftEssentials.commands;

import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.PiricraftEssentials;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommands implements CommandExecutor {
    private final PiricraftEssentials plugin;
    private final EssentialsDatabaseManager db;
    private final TeleportManager teleports;

    public TeleportCommands(PiricraftEssentials plugin, EssentialsDatabaseManager db, TeleportManager teleports) {
        this.plugin = plugin;
        this.db = db;
        this.teleports = teleports;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String action = command.getName().toLowerCase();
        if (action.equals("tp")) {
            if (!sender.hasPermission("piricraft.essentials.tp")) { sender.sendMessage(TextUtils.color("<red>Permission insuffisante.")); return true; }
            if (args.length == 1 && sender instanceof Player player) {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) { sender.sendMessage(TextUtils.color("<red>Joueur introuvable.")); return true; }
                teleports.teleportWithWarmup(player, target.getLocation(), 0, null);
            } else if (args.length >= 2) {
                Player target = Bukkit.getPlayerExact(args[0]);
                Player destination = Bukkit.getPlayerExact(args[1]);
                if (target == null || destination == null) { sender.sendMessage(TextUtils.color("<red>Joueur introuvable.")); return true; }
                teleports.teleportWithWarmup(target, destination.getLocation(), 0, null);
            } else sender.sendMessage(TextUtils.color("<red>Usage: /tp <joueur> [destination]</red>"));
            return true;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage("Cette commande est réservée aux joueurs."); return true; }
        EssentialsProfile profile = db.getProfileFromCache(player.getUniqueId());
        if (profile == null) return true;
        switch (action) {
            case "tpa" -> {
                if (args.length == 0) { player.sendMessage(TextUtils.color("<red>Usage: /tpa <joueur></red>")); break; }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null || target.equals(player)) { player.sendMessage(TextUtils.color("<red>Joueur introuvable.")); break; }
                teleports.sendTpaRequest(player, target);
            }
            case "tpaccept", "tpdeny" -> {
                var requests = profile.getTpaRequests();
                requests.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
                if (requests.isEmpty()) { player.sendMessage(TextUtils.color("<red>Aucune demande en attente.")); break; }
                Player requester = null;
                if (args.length > 0) {
                    Player requested = Bukkit.getPlayerExact(args[0]);
                    if (requested != null && requests.containsKey(requested.getUniqueId())) requester = requested;
                } else requester = Bukkit.getPlayer(requests.keySet().iterator().next());
                if (requester == null) { player.sendMessage(TextUtils.color("<red>Le joueur n'est plus en ligne.")); break; }
                requests.remove(requester.getUniqueId());
                if (action.equals("tpaccept")) {
                    Location destination = player.getLocation();
                    teleports.teleportWithWarmup(requester, destination, plugin.getConfig().getInt("teleportation.warmup-seconds", 3), () -> player.sendMessage(TextUtils.color("<green>Demande acceptée.")));
                    requester.sendMessage(TextUtils.color("<green>Votre demande a été acceptée."));
                } else { requester.sendMessage(TextUtils.color("<red>Votre demande a été refusée.")); player.sendMessage(TextUtils.color("<yellow>Demande refusée.")); }
            }
            case "back" -> {
                Location last = profile.getLastLocation();
                if (last == null || last.getWorld() == null) player.sendMessage(TextUtils.color("<red>Aucune position précédente enregistrée."));
                else teleports.teleportWithWarmup(player, last, plugin.getConfig().getInt("teleportation.warmup-seconds", 3), null);
            }
            case "spawn" -> {
                Location spawn = player.getWorld().getSpawnLocation();
                teleports.teleportWithWarmup(player, spawn, plugin.getConfig().getInt("teleportation.warmup-seconds", 3), null);
            }
        }
        return true;
    }
}
