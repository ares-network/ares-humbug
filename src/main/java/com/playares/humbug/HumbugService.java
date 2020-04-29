package com.playares.humbug;

import com.playares.humbug.cont.ModManager;
import com.playares.commons.AresPlugin;
import com.playares.commons.AresService;
import lombok.Getter;

public final class HumbugService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter public final String name = "Humbug Service";
    @Getter protected ModManager modManager;

    public HumbugService(AresPlugin owner) {
        this.owner = owner;
    }

    public void start() {
        this.modManager = new ModManager(this);
        modManager.load();
        owner.registerCommand(new HumbugCommand(this));
    }

    public void stop() {
        modManager.unload();
    }
}
