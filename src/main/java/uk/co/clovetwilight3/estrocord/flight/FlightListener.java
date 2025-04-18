/*
 * Copyright (c) 2025 Mazey-Jessica Emily Twilight
 * Copyright (c) 2025 UnifiedGaming Systems Ltd (Company Number: 16108983)
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package uk.co.clovetwilight3.estrocord.flight;

import uk.co.clovetwilight3.estrocord.EstrocordPlugin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class FlightListener implements Listener {

    private final BaseFlightMain flightMain;
    private final EstrocordPlugin estrocordPlugin;

    public FlightListener(BaseFlightMain flightMain, EstrocordPlugin estrocordPlugin) {
        this.flightMain = flightMain;
        this.estrocordPlugin = estrocordPlugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (estrocordPlugin.getConfig().getBoolean("AllFlight")) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.sendMessage(ChatColor.GREEN + "Flight enabled globally.");
            }
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
            return;
        }

        Location playerLocation = player.getLocation();
        boolean inCommunalZone = false;

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
            return;
        }

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
