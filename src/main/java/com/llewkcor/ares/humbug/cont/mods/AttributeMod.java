package com.llewkcor.ares.humbug.cont.mods;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.humbug.Humbug;
import com.llewkcor.ares.humbug.cont.HumbugMod;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class AttributeMod implements HumbugMod, Listener {
    @Getter public final Humbug plugin;
    @Getter public final String name = "Attribute Hider";
    @Getter @Setter public boolean enabled;

    public AttributeMod(Humbug plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    @Override
    public void load() {
        if (enabled) {
            return;
        }

        final YamlConfiguration config = Configs.getConfig(plugin, "config");
        this.enabled = config.getBoolean("hide_attributes.enabled");

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final ItemStack item = packet.getItemModifier().read(0);

                if (item == null) {
                    return;
                }

                if (!isObfuscatable(item.getType())) {
                    final ItemMeta meta = item.getItemMeta();

                    if (meta != null) {
                        meta.setLore(null);
                        meta.setDisplayName(null);
                        item.setItemMeta(meta);
                    }

                    packet.getItemModifier().write(0, item);
                }
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final StructureModifier<Integer> ints = packet.getIntegers();

                if (event.getPlayer().getEntityId() == ints.read(0)) {
                    return;
                }

                packet.getBytes().write(1, (byte)0);
                ints.write(1, 0);
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final PacketContainer packet = event.getPacket();
                final Player player = event.getPlayer();
                final Entity entity = packet.getEntityModifier(event).read(0);
                final StructureModifier<List<WrappedWatchableObject>> modifier = packet.getWatchableCollectionModifier();
                final List<WrappedWatchableObject> read = modifier.read(0);

                if (
                        player.getUniqueId().equals(entity.getUniqueId()) ||
                        !(entity instanceof LivingEntity) ||
                        entity instanceof EnderDragon ||
                        entity instanceof Wither ||
                        entity.getPassenger().equals(player)) {

                    return;

                }

                for (WrappedWatchableObject obj : read) {
                    if (obj.getIndex() == 7) {
                        final float value = (float)obj.getValue();

                        if (value > 0) {
                            obj.setValue(1F);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void unload() {

    }

    private boolean isObfuscatable(Material type) {
        return type == Material.DIAMOND_HELMET
                || type == Material.DIAMOND_CHESTPLATE
                || type == Material.DIAMOND_LEGGINGS
                || type == Material.DIAMOND_BOOTS
                || type == Material.IRON_HELMET
                || type == Material.IRON_CHESTPLATE
                || type == Material.IRON_LEGGINGS
                || type == Material.IRON_BOOTS
                || type == Material.GOLD_HELMET
                || type == Material.GOLD_CHESTPLATE
                || type == Material.GOLD_LEGGINGS
                || type == Material.GOLD_BOOTS
                || type == Material.LEATHER_HELMET
                || type == Material.LEATHER_CHESTPLATE
                || type == Material.LEATHER_LEGGINGS
                || type == Material.LEATHER_BOOTS
                || type == Material.DIAMOND_SWORD
                || type == Material.GOLD_SWORD
                || type == Material.IRON_SWORD
                || type == Material.STONE_SWORD
                || type == Material.WOOD_SWORD
                || type == Material.DIAMOND_AXE
                || type == Material.GOLD_AXE
                || type == Material.IRON_AXE
                || type == Material.STONE_AXE
                || type == Material.WOOD_AXE
                || type == Material.DIAMOND_PICKAXE
                || type == Material.GOLD_PICKAXE
                || type == Material.IRON_PICKAXE
                || type == Material.STONE_PICKAXE
                || type == Material.WOOD_PICKAXE
                || type == Material.DIAMOND_SPADE
                || type == Material.GOLD_SPADE
                || type == Material.IRON_SPADE
                || type == Material.STONE_SPADE
                || type == Material.WOOD_SPADE
                || type == Material.FIREWORK
                || type == Material.WRITTEN_BOOK
                || type == Material.ENCHANTED_BOOK;
    }
}
