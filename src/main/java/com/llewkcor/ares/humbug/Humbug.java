package com.llewkcor.ares.humbug;

import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.humbug.cont.ModManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Humbug extends JavaPlugin {
    @Getter protected Ares core;
    @Getter protected ModManager modManager;

    @Override
    public void onEnable() {
        this.core = (Ares) Bukkit.getPluginManager().getPlugin("ares-core");
        this.modManager = new ModManager(this);

        modManager.load();
    }

    @Override
    public void onDisable() {
        modManager.unload();
    }
}
