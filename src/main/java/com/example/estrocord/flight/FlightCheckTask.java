package com.example.estrocord.flight;

import com.example.estrocord.EstrocordPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class FlightCheckTask extends BukkitRunnable {

    private final BaseFlightMain flightMain;
    private final EstrocordPlugin estrocordPlugin;

    public FlightCheckTask(BaseFlightMain flightMain, EstrocordPlugin estrocordPlugin) {
        this.flightMain = flightMain;
        this.estrocordPlugin = estrocordPlugin;
    }

    @Override
    public void run() {
        boolean allFlightEnabled = estrocordPlugin.isAllFlightEnabled();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();

            // Check if allflight is enabled
            if (allFlightEnabled) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                    player.sendMessage(ChatColor.GREEN + "Flight enabled globally.");
                }
                continue;
            }

            // Skip checks for players in spectator or creative mode
            if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                }
                continue;
            }

            Location playerLocation = player.getLocation();
            boolean inCommunalZone = false;

            // Check if the player is within any communal fly zone
            for (BaseFlightMain.FlyZone zone : flightMain.getCommunalFlyZones().values()) {
                if (zone.isWithinZone(playerLocation)) {
                    inCommunalZone = true;
                    break;
                }
            }

            if (inCommunalZone) {
                if (!player.getAllowFlight()) {
                    player.setAllowFlight(true);
                    player.sendMessage(ChatColor.GREEN + "Flight enabled within communal fly zone.");
                }
                continue;
            }

            // Check if the player is within their base radius
            Location baseLocation = estrocordPlugin.getBaseLocation(playerUUID);
            if (baseLocation != null && baseLocation.getWorld().equals(playerLocation.getWorld())) {
                double distance = baseLocation.distance(playerLocation);
                boolean withinRadius = distance <= 100;

                if (withinRadius && flightMain.getFlightToggles().getOrDefault(playerUUID, false)) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                        player.sendMessage(ChatColor.GREEN + "Flight enabled within base radius.");
                    }
                } else if (!withinRadius && player.getAllowFlight()) {
                    player.setAllowFlight(false);
                    player.sendMessage(ChatColor.RED + "Flight disabled. You left the base radius.");
                }
            } else if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.sendMessage(ChatColor.RED + "Flight disabled. You do not have a valid base.");
            }
        }
    }
}
