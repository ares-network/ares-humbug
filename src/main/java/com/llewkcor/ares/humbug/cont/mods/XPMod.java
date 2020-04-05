package com.llewkcor.ares.humbug.cont.mods;

import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public final class XPMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Experience";
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean initialized;

    public XPMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;
        this.initialized = false;
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(plugin, "config");

        this.enabled = config.getBoolean("mods.xp.enabled");

        if (initialized) {
            return;
        }

        final ItemStack expBottle = new ItemStack(Material.EXP_BOTTLE);
        final ShapedRecipe recipe = new ShapedRecipe(expBottle);

        recipe.shape("*");
        recipe.setIngredient('*', Material.EMERALD);

        plugin.getServer().addRecipe(recipe);

        this.initialized = true;
    }

    @Override
    public void unload() {}
}