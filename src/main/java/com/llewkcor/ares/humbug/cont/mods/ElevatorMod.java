package com.llewkcor.ares.humbug.cont.mods;

import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public final class ElevatorMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Elevators";
    @Getter @Setter public boolean enabled;

    public ElevatorMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        final YamlConfiguration config = Configs.getConfig(plugin, "config");

        this.enabled = config.getBoolean("mods.elevator.enabled");
    }

    @Override
    public void unload() {
        this.enabled = false;
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (isElevator(event.getVehicle())) {
            final Location destination = getDestination(event.getVehicle().getLocation());

            if (destination != null) {
                destination.add(0.5, 0.0, 0.5);
                destination.setYaw(event.getExited().getLocation().getYaw());
                destination.setPitch(event.getExited().getLocation().getPitch());

                new Scheduler(plugin).sync(() -> event.getExited().teleport(destination)).delay(1L).run();
            }
        }
    }

    /**
     * Returns true if the provided vehicle is an Elevator
     * @param vehicle Bukkit Vehicle
     * @return True if Elevator
     */
    private boolean isElevator(Vehicle vehicle) {
        if (!(vehicle instanceof Minecart)) {
            return false;
        }

        final Block above = vehicle.getLocation().getBlock().getRelative(BlockFace.UP);

        return above != null && above.getType().isBlock();
    }

    /**
     * Returns the next stable location above the provided location
     * @param location Bukkit Location
     * @return Bukkit Location
     */
    private Location getDestination(Location location) {
        for (int y = location.getBlockY(); y < location.getWorld().getMaxHeight(); y++) {
            final Block block = location.getWorld().getBlockAt(location.getBlockX(), y, location.getBlockZ());

            if (block != null && block.getType().equals(Material.AIR)) {
                return block.getLocation();
            }
        }

        return null;
    }
}