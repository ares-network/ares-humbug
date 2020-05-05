package com.playares.humbug.cont;

import com.google.common.collect.Maps;
import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.mods.*;
import lombok.Getter;

import java.util.Map;

public final class ModManager {
    @Getter public HumbugService plugin;
    @Getter public Map<Class<? extends HumbugMod>, HumbugMod> mods;

    public ModManager(HumbugService plugin) {
        this.plugin = plugin;
        this.mods = Maps.newConcurrentMap();

        mods.put(WorldMod.class, new WorldMod(plugin));
        mods.put(ElevatorMod.class, new ElevatorMod(plugin));
        mods.put(MobstackMod.class, new MobstackMod(plugin));
        mods.put(KitMod.class, new KitMod(plugin));
        mods.put(KnockbackMod.class, new KnockbackMod(plugin));
        mods.put(PotionMod.class, new PotionMod(plugin));
        mods.put(XPMod.class, new XPMod(plugin));
        mods.put(MiningMod.class, new MiningMod(plugin));
        mods.put(BotMod.class, new BotMod(plugin));
        mods.put(ChatMod.class, new ChatMod(plugin));
        mods.put(ExploitMod.class, new ExploitMod(plugin));
        mods.put(DurabilityMod.class, new DurabilityMod(plugin));
        mods.put(AttributeMod.class, new AttributeMod(plugin));
    }

    public HumbugMod getMod(Class<? extends HumbugMod> clazz) {
        return mods.get(clazz);
    }

    public void load() {
        mods.values().forEach(HumbugMod::load);
    }

    public void unload() {
        mods.values().forEach(HumbugMod::unload);
    }

    public void reload() {
        mods.values().forEach(HumbugMod::reload);
    }
}
