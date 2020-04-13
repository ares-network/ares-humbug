package com.llewkcor.ares.humbug.cont.mods;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public final class XPMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Experience";
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean initialized;

    @Getter @Setter public boolean bottleExpEnabled;
    @Getter @Setter public double baseExpMultiplier;
    @Getter @Setter public boolean spawnersDisabled;
    @Getter @Setter public boolean lootingFortuneMultiplierEnabled;

    public XPMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;
        this.initialized = false;
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(plugin, "config");

        this.enabled = true;
        this.bottleExpEnabled = config.getBoolean("mods.xp.bottle_exp_enabled");
        this.spawnersDisabled = config.getBoolean("mods.exp.disable_monster_spawners");
        this.baseExpMultiplier = config.getDouble("mods.xp.exp_base_multiplier");
        this.lootingFortuneMultiplierEnabled = config.getBoolean("mods.exp.looting_fortune_multiplier_enabled");

        if (initialized) {
            return;
        }

        final ItemStack expBottle = new ItemStack(Material.EXP_BOTTLE);
        final ShapedRecipe recipe = new ShapedRecipe(expBottle);

        recipe.shape("*");
        recipe.setIngredient('*', Material.EMERALD);

        plugin.getServer().addRecipe(recipe);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommandManager().registerCommand(new BottleCommand());

        this.initialized = true;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!isEnabled() || !isBottleExpEnabled() || event.isCancelled()) {
            return;
        }

        final Projectile projectile = event.getEntity();
        final ProjectileSource shooter = event.getEntity().getShooter();

        if (!(projectile instanceof ThrownExpBottle) || !(shooter instanceof Player)) {
            return;
        }

        final Player player = (Player)shooter;
        final ItemStack bottle =  player.getItemInHand();

        if (bottle == null || !bottle.getType().equals(Material.EXP_BOTTLE)) {
            return;
        }

        final ItemMeta meta = bottle.getItemMeta();
        final List<String> lore = meta.getLore();

        if (lore == null || lore.size() != 1) {
            return;
        }

        // 345 exp
        final String line = lore.get(0);
        final String expNumber = ChatColor.stripColor(line.replace(" exp", ""));
        int exp = 0;

        try {
            exp = Integer.parseInt(expNumber);
        } catch (NumberFormatException ex) {
            return;
        }

        event.setCancelled(true);

        if (bottle.getAmount() <= 1) {
            player.getInventory().removeItem(bottle);
        } else {
            bottle.setAmount(bottle.getAmount() - 1);
        }

        player.setLevel(player.getLevel() + exp);
        player.sendMessage(ChatColor.GREEN + "You consumed " + ChatColor.AQUA + exp + " levels" + ChatColor.GREEN + " of Experience");
    }

    @Override
    public void unload() {}

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled() || !isLootingFortuneMultiplierEnabled() || event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            return;
        }

        final int enchantMultiplier = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        final int xp = event.getExpToDrop();

        event.setExpToDrop((int)Math.round((xp * (enchantMultiplier * baseExpMultiplier))));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled() || !isLootingFortuneMultiplierEnabled()) {
            return;
        }
        
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        final ItemStack hand = killer.getItemInHand();

        if (hand == null || !hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            return;
        }

        final int enchantMultiplier = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        final int xp = event.getDroppedExp();

        event.setDroppedExp((int)Math.round((xp * (enchantMultiplier * baseExpMultiplier))));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isEnabled() || isSpawnersDisabled() || event.isCancelled()) {
            return;
        }

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.setCancelled(true);
        }
    }

    public final class BottleCommand extends BaseCommand {
        @CommandAlias("bottle")
        @Description("Bottle all of your current EXP")
        public void onBottle(Player player) {
            final int levels = player.getLevel();
            final ItemStack hand = player.getItemInHand();

            if (!isBottleExpEnabled()) {
                player.sendMessage(ChatColor.RED + "Bottling Experience has been disabled");
                return;
            }

            if (levels <= 0) {
                player.sendMessage(ChatColor.RED + "You need at least 1 level to bottle experience");
                return;
            }

            if (hand == null || !hand.getType().equals(Material.GLASS_BOTTLE)) {
                player.sendMessage(ChatColor.RED + "You are not holding an empty glass bottle");
                return;
            }

            final ItemStack item = new ItemBuilder()
                    .setMaterial(Material.EXP_BOTTLE)
                    .addLore(ChatColor.DARK_PURPLE + "" + levels + " exp")
                    .build();

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
                player.sendMessage(ChatColor.RED + "The Experience Bottle was dropped at your feet because your inventory is full");
                return;
            }

            if (hand.getAmount() <= 1) {
                player.getInventory().removeItem(hand);
            } else {
                hand.setAmount(hand.getAmount() - 1);
            }

            player.getInventory().addItem(item);
            player.setLevel(0);
            player.sendMessage(ChatColor.GREEN + "You have bottled " + levels + " levels of Experience");
        }
    }
}