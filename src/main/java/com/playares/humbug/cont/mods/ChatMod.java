package com.playares.humbug.cont.mods;

import com.google.common.collect.Maps;
import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.HumbugMod;
import com.playares.commons.event.ProcessedChatEvent;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Configs;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ChatMod implements HumbugMod, Listener {
    @Getter public final HumbugService humbug;
    @Getter public final String name = "Chat";
    @Getter @Setter public boolean enabled;

    @Getter public final Map<UUID, Long> recentChatters;

    @Getter public boolean hideJoinLeaveMessages;
    @Getter public boolean disablePostingLinks;
    @Getter public List<String> whitelistedLinks;
    @Getter public boolean rateLimitChatEnabled;
    @Getter public int chatRateLimit;

    public ChatMod(HumbugService humbug) {
        this.humbug = humbug;
        this.enabled = false;
        this.recentChatters = Maps.newConcurrentMap();

        humbug.getOwner().registerListener(this);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(humbug.getOwner(), "humbug");

        this.hideJoinLeaveMessages = config.getBoolean("mods.chat.hide_join_leave_messages");
        this.disablePostingLinks = config.getBoolean("mods.chat.disable_posting_links");
        this.whitelistedLinks = config.getStringList("mods.chat.allowed_links");
        this.rateLimitChatEnabled = config.getBoolean("mods.chat.chat_cooldowns.enabled");
        this.chatRateLimit = config.getInt("mods.chat.rate_limit");

        this.enabled = true;
    }

    @Override
    public void unload() {
        this.enabled = false;
    }

    /**
     * Returns a time in milliseconds when the player can chat again
     * @param player Player
     * @return Time in ms
     */
    private long getRemainingChatCooldown(Player player) {
        return recentChatters.getOrDefault(player.getUniqueId(), 0L);
    }

    /**
     * Returns true if the provided string is a blacklisted link
     * @param message Message
     * @return True if blacklisted
     */
    private boolean isBlacklistedLink(String message) {
        final boolean match = message.matches("^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");

        for (String whitelisted : whitelistedLinks) {
            if (message.contains(whitelisted)) {
                return false;
            }
        }

        return match;
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPostLink(ProcessedChatEvent event) {
        if (!isEnabled() || !isDisablePostingLinks() || event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final String message = event.getMessage();
        final String[] split = message.split(" ");

        if (player.hasPermission("humbug.chat.bypass")) {
            return;
        }

        for (String str : split) {
            if (isBlacklistedLink(str)) {

                player.sendMessage(ChatColor.RED + "This type of link is blacklisted for non-premium users.");
                player.sendMessage(ChatColor.YELLOW + "To bypass this filter purchase a premium rank at " + ChatColor.AQUA + "https://playares.com/store");

                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onChat(ProcessedChatEvent event) {
        if (!isEnabled() || !isRateLimitChatEnabled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission("humbug.chat.bypass")) {
            return;
        }

        final long remainingCooldown = getRemainingChatCooldown(player);

        if (remainingCooldown > Time.now()) {
            player.sendMessage(ChatColor.RED + "Please wait " + (Time.convertToDecimal(remainingCooldown - Time.now()) + "s before sending another message."));
            player.sendMessage(ChatColor.YELLOW + "To bypass this filter purchase a premium rank at " + ChatColor.AQUA + "https://playares.com/store");

            event.setCancelled(true);
            return;
        }

        final UUID uniqueId = player.getUniqueId();
        final long nextAllowedMessage = (Time.now() + (chatRateLimit * 1000L));

        recentChatters.put(player.getUniqueId(), nextAllowedMessage);
        new Scheduler(humbug.getOwner()).async(() -> recentChatters.remove(uniqueId)).delay(chatRateLimit * 20L).run();
    }
}