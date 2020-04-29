package com.playares.humbug.cont;

import com.google.common.collect.Sets;
import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.mods.*;
import lombok.Getter;

import java.util.Set;

public final class ModManager {
    @Getter public HumbugService plugin;
    @Getter public Set<HumbugMod> mods;

    public ModManager(HumbugService plugin) {
        this.plugin = plugin;
        this.mods = Sets.newConcurrentHashSet();

        mods.add(new WorldMod(plugin));
        mods.add(new ElevatorMod(plugin));
        mods.add(new MobstackMod(plugin));
        mods.add(new KitMod(plugin));
        mods.add(new KnockbackMod(plugin));
        mods.add(new PotionMod(plugin));
        mods.add(new XPMod(plugin));
        mods.add(new MiningMod(plugin));
        mods.add(new BotMod(plugin));
        mods.add(new ChatMod(plugin));
        mods.add(new ExploitMod(plugin));
        mods.add(new DurabilityMod(plugin));
        mods.add(new AttributeMod(plugin));
    }

    public void load() {
        mods.forEach(HumbugMod::load);
    }

    public void unload() {
        mods.forEach(HumbugMod::unload);
    }

    public void reload() {
        mods.forEach(HumbugMod::reload);
    }
}
