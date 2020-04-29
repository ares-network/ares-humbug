package com.playares.humbug.cont.mods;

import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.HumbugMod;
import com.playares.commons.util.general.Configs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class DurabilityMod implements HumbugMod, Listener {
    @Getter public final HumbugService humbug;
    @Getter public final String name = "Durability";
    @Getter @Setter public boolean enabled;

    public DurabilityMod(HumbugService humbug) {
        this.humbug = humbug;
        this.enabled = false;

        humbug.getOwner().registerListener(this);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(humbug.getOwner(), "humbug");

        this.enabled = config.getBoolean("mods.armor_durability.enabled");
    }

    @Override
    public void unload() {
        this.enabled = false;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !isEnabled()) {
            return;
        }

        final Entity damaged = event.getEntity();
        final Entity damager = event.getDamager();

        if (!(damaged instanceof Player)) {
            return;
        }

        int sharpness = 0;

        if (damager instanceof Player) {
            final Player damagerPlayer = (Player)damager;
            final ItemStack hand = damagerPlayer.getItemInHand();

            if (hand != null && hand.hasItemMeta()) {
                sharpness = hand.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL);
            }
        }

        final Player player = (Player)damaged;

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || armor.getType().equals(Material.AIR) || !armor.hasItemMeta()) {
                continue;
            }

            final int unbreaking = armor.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
            int toAdd = sharpness - unbreaking;

            if ((toAdd - 1) <= 0) {
                continue;
            }

            armor.setDurability((short)(armor.getDurability() + (toAdd - 1)));
        }
    }
}