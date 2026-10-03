package fr.piricraft.piricraftEssentials.models;

import org.bukkit.Location;

public class UserHome {

    private final String name;
    private final Location location;
    private final long createdAt;

    public UserHome(String name, Location location, long createdAt) {
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
