package com.llewkcor.ares.humbug.cont.mods;

import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PotionMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Potions";
    @Getter @Setter public boolean enabled;

    @Getter public boolean oldHealthEnabled;
    @Getter public boolean oldRegenEnabled;
    @Getter public boolean oldStrengthEnabled;

    public PotionMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(plugin, "config");

        this.enabled = config.getBoolean("mods.potions.enabled");
        this.oldHealthEnabled = config.getBoolean("mods.potions.old_health");
        this.oldRegenEnabled = config.getBoolean("mods.potions.old_regen");
        this.oldStrengthEnabled = config.getBoolean("mods.potions.old_strength");
    }

    @Override
    public void unload() {

    }

    private double calculateFinalDamage(Player player, double init) {
        double damage = init;

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                damage += (effect.getAmplifier() * 1.3);
                continue;
            }

            if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                damage += (effect.getAmplifier() * -0.5);
            }
        }

        return damage;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!isEnabled() || !isOldStrengthEnabled()) {
            return;
        }

        final Entity damager = event.getDamager();

        if (!(damager instanceof Player)) {
            return;
        }

        event.setDamage(calculateFinalDamage((Player)damager, event.getDamage()));
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!isEnabled() || (!isOldRegenEnabled() && !isOldHealthEnabled())) {
            return;
        }

        final LivingEntity entity = (LivingEntity)event.getEntity();
        int level = 0;

        for (PotionEffect effect : entity.getActivePotionEffects()) {
            final PotionEffectType type = effect.getType();
            final int amplifier = effect.getAmplifier();

            if (type.equals(PotionEffectType.REGENERATION) || type.equals(PotionEffectType.HEAL)) {
                level = amplifier + 1;
                break;
            }
        }

        final EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        final double amount = event.getAmount();

        if (isOldHealthEnabled() && reason.equals(EntityRegainHealthEvent.RegainReason.MAGIC) && amount > 1.0 && level >= 0) {
            event.setAmount(amount * 1.5);
            return;
        }

        if (isOldRegenEnabled() && reason.equals(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN) && amount == 1.0 && level > 0) {
            new Scheduler(plugin).sync(() -> {
                if (entity.isDead()) {
                    return;
                }

                final double max = entity.getMaxHealth();
                final double current = entity.getHealth();

                if (max >= current) {
                    return;
                }

                // entity.setHealth((max >= current + 1.0) ? current + 1.0 : max);
                entity.setHealth(Math.min(max, current + 1.0));
            }).delay(50L / (level * 2)).run();
        }
    }
}
