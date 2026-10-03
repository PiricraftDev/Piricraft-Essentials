package fr.piricraft.piricraftEssentials.models;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EssentialsProfile {

    private final UUID playerUuid;
    private final Map<String, UserHome> homes;
    private Location lastLocation;
    private boolean godMode;
    private boolean vanished;
    private boolean socialSpy;
    private final Map<UUID, Long> tpaRequests;

    public EssentialsProfile(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.homes = new ConcurrentHashMap<>();
        this.tpaRequests = new ConcurrentHashMap<>();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Map<String, UserHome> getHomes() {
        return homes;
    }

    public void addHome(UserHome home) {
        homes.put(home.getName().toLowerCase(), home);
    }

    public void removeHome(String name) {
        homes.remove(name.toLowerCase());
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }

    public boolean isSocialSpy() {
        return socialSpy;
    }

    public void setSocialSpy(boolean socialSpy) {
        this.socialSpy = socialSpy;
    }

    public void addTpaRequest(UUID senderUuid, long expireAt) {
        tpaRequests.put(senderUuid, expireAt);
    }

    public boolean hasTpaFrom(UUID senderUuid) {
        Long expireAt = tpaRequests.get(senderUuid);
        if (expireAt == null) {
            return false;
        }

        if (System.currentTimeMillis() > expireAt) {
            tpaRequests.remove(senderUuid);
            return false;
        }

        return true;
    }

    public Map<UUID, Long> getTpaRequests() {
        return tpaRequests;
    }
}