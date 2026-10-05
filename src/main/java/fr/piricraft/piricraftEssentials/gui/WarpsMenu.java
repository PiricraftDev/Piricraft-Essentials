package fr.piricraft.piricraftEssentials.gui;

import fr.piricraft.piricraftCore.managers.MenuManager;
import fr.piricraft.piricraftCore.models.PiricraftMenu;
import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.managers.EssentialsDatabaseManager;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.UserWarp;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WarpsMenu extends PiricraftMenu {
    private final EssentialsDatabaseManager db;
    private final TeleportManager teleports;
    private final List<String> slots = new ArrayList<>();
    private final int warmupSeconds;

    public WarpsMenu(EssentialsDatabaseManager db, TeleportManager teleports, int warmupSeconds) {
        super(TextUtils.color("<green><bold>Warps</bold></green>"), 54);
        this.db = db;
        this.teleports = teleports;
        this.warmupSeconds = warmupSeconds;
    }

    @Override
    public void setMenuItems(Player player) {
        slots.clear();
        for (UserWarp warp : db.getWarpCache().values()) {
            if (warp.getPermission() != null && !player.hasPermission(warp.getPermission())) continue;
            if (slots.size() >= inventory.getSize()) break;
            slots.add(warp.getName());
            List<Component> lore = List.of(TextUtils.color("<gray>Monde: <yellow>" + (warp.getLocation().getWorld() == null ? "Inconnu" : warp.getLocation().getWorld().getName()) + "</yellow></gray>"), TextUtils.color("<green>Clic pour vous téléporter</green>"));
            inventory.setItem(slots.size() - 1, MenuManager.createItem(warp.getIcon(), TextUtils.color("<gold>" + warp.getName() + "</gold>"), lore));
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!(event.getWhoClicked() instanceof Player player) || slot < 0 || slot >= event.getView().getTopInventory().getSize() || slot >= slots.size()) return;
        UserWarp warp = db.getWarp(slots.get(slot));
        if (warp == null || (warp.getPermission() != null && !player.hasPermission(warp.getPermission()))) return;
        player.closeInventory();
        teleports.teleportWithWarmup(player, warp.getLocation(), warmupSeconds, null);
    }
}
