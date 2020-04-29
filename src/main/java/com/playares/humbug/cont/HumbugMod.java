package com.playares.humbug.cont;

import com.playares.humbug.HumbugService;

public interface HumbugMod {
    /**
     * Returns the Humbug plugin instance
     * @return Humbug
     */
    HumbugService getHumbug();

    /**
     * Returns the name of this Humbug Module
     * @return Name
     */
    String getName();

    /**
     * Returns true if this Humbug Module is enabled
     * @return True if enabled
     */
    boolean isEnabled();

    /**
     * Sets the module enabled/disabled
     * @param bool Enabled value
     */
    void setEnabled(boolean bool);

    /**
     * Loads this Humbug Module
     */
    void load();

    /**
     * Unloads this Humbug Module
     */
    void unload();

    /**
     * Reloads this Humbug module
     */
    default void reload() {
        load();
    }
}