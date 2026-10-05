package fr.piricraft.piricraftEssentials.models;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class UserWarp {

    private final String name;
    private final UUID ownerUuid;
    private final Location location;
    private final Material icon;
    private final String permission;

    public UserWarp(String name, UUID ownerUuid, Location location, Material icon, String permission) {
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.location = location.clone();
        this.icon = icon;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Material getIcon() {
        return icon;
    }

    public String getPermission() {
        return permission;
    }
}
