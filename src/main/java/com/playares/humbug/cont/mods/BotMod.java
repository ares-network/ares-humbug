package com.playares.humbug.cont.mods;

import com.google.common.collect.Lists;
import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.HumbugMod;
import com.playares.commons.util.general.Configs;
import com.playares.commons.util.general.IPS;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.stream.Collectors;

public final class BotMod implements HumbugMod, Listener {
    @Getter public final HumbugService humbug;
    @Getter public final String name = "Bot Protection";
    @Getter @Setter public boolean enabled;

    @Getter public boolean limitConnections;
    @Getter public int maxConnectionsPerIP;

    public BotMod(HumbugService humbug) {
        this.humbug = humbug;
        humbug.getOwner().registerListener(this);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(humbug.getOwner(), "humbug");

        this.limitConnections = config.getBoolean("mods.bots.limit_connections");
        this.maxConnectionsPerIP = config.getInt("mods.bots.max_conn_per_ip");

        this.enabled = true;
    }

    @Override
    public void unload() {
        this.enabled = false;
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final long address = IPS.toLong(event.getAddress().getHostAddress());

        if (isEnabled() && isLimitConnections() && getOpenConnections(address) > getMaxConnectionsPerIP()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Too many accounts connected with your IP address");
        }
    }

    /**
     * Returns a count of how many other players are connected with the same IP Address
     * @param address Address
     * @return Amount of connected IPS that match the provided IP
     */
    private int getOpenConnections(long address) {
        final List<Long> addresses = Lists.newArrayList();

        Bukkit.getOnlinePlayers().forEach(player -> addresses.add(IPS.toLong(player.getAddress().getAddress().getHostAddress())));

        final List<Long> match = addresses.stream().filter(addr -> addr == address).collect(Collectors.toList());

        return (match.size() + 1);
    }
}
