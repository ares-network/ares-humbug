package com.llewkcor.ares.humbug.cont.mods;

import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ChatMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Chat";
    @Getter @Setter public boolean enabled;

    @Getter public boolean hideJoinLeaveMessages;

    public ChatMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(plugin, "config");

        this.enabled = config.getBoolean("mods.chat.enabled");
        this.hideJoinLeaveMessages = config.getBoolean("mods.chat.hide_join_leave_messages");
    }

    @Override
    public void unload() {
        this.enabled = false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (isEnabled() && isHideJoinLeaveMessages()) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isEnabled() && isHideJoinLeaveMessages()) {
            event.setQuitMessage(null);
        }
    }
}