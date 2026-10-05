package fr.piricraft.piricraftEssentials.gui;

import fr.piricraft.piricraftCore.managers.MenuManager;
import fr.piricraft.piricraftCore.models.PiricraftMenu;
import fr.piricraft.piricraftCore.utils.TextUtils;
import fr.piricraft.piricraftEssentials.managers.TeleportManager;
import fr.piricraft.piricraftEssentials.models.EssentialsProfile;
import fr.piricraft.piricraftEssentials.models.UserHome;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HomesMenu extends PiricraftMenu {

    private final EssentialsProfile profile;
    private final TeleportManager teleportManager;
    private final int warmupSeconds;

    public HomesMenu(EssentialsProfile profile, TeleportManager teleportManager, int warmupSeconds) {
        super(TextUtils.color("<green><bold>Mes Homes</bold></green>"), 27);
        this.profile = profile;
        this.teleportManager = teleportManager;
        this.warmupSeconds = warmupSeconds;
    }

    @Override
    public void setMenuItems(Player player) {
        int slot = 0;
        for (UserHome home : profile.getHomes().values()) {
            if (slot >= inventory.getSize()) break;

            List<Component> loreComponents = List.of(
                    TextUtils.color("<gray>Monde : <yellow>" + (home.getLocation().getWorld() != null ? home.getLocation().getWorld().getName() : "Inconnu") + "</yellow></gray>"),
                    TextUtils.color("<gray>X: <yellow>" + home.getLocation().getBlockX() + "</yellow> Y: <yellow>" + home.getLocation().getBlockY() + "</yellow> Z: <yellow>" + home.getLocation().getBlockZ() + "</yellow></gray>"),
                    TextUtils.color(""),
                    TextUtils.color("<green>âž” Clic pour vous tÃ©lÃ©porter</green>")
            );

            ItemStack item = MenuManager.createItem(
                    Material.RED_BED,
                    TextUtils.color("<gold>" + home.getName() + "</gold>"),
                    loreComponents
            );

            inventory.setItem(slot++, item);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.displayName() == null) return;

        String homeName = PlainTextComponentSerializer.plainText().serialize(meta.displayName()).toLowerCase(java.util.Locale.ROOT);
        UserHome home = profile.getHomes().get(homeName.toLowerCase());

        if (home != null) {
            player.closeInventory();
            teleportManager.teleportWithWarmup(player, home.getLocation(), warmupSeconds, () -> {
                player.sendMessage(TextUtils.color("<green>[TÃ©lÃ©portation]</green> Vous avez Ã©tÃ© tÃ©lÃ©portÃ© Ã  votre home <gold>" + home.getName() + "</gold>."));
            });
        }
    }
}
