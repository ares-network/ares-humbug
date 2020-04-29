package com.playares.humbug.cont.mods;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.playares.humbug.HumbugService;
import com.playares.humbug.cont.HumbugMod;
import com.playares.commons.util.general.Configs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;
import java.util.Random;

public final class AttributeMod implements HumbugMod, Listener {
    @Getter public final HumbugService humbug;
    @Getter public final String name = "Attribute Hider";
    @Getter @Setter public boolean enabled;
    @Getter public final Random random;

    public AttributeMod(HumbugService humbug) {
        this.humbug = humbug;
        this.enabled = false;
        this.random = new Random();
    }

    @Override
    public void load() {
        if (enabled) {
            return;
        }

        final YamlConfiguration config = Configs.getConfig(humbug.getOwner(), "humbug");
        this.enabled = config.getBoolean("mods.hide_attributes.enabled");

        // We check twice because it can be disabled in the config from here
        if (!enabled) {
            return;
        }

        //Strips armour
        humbug.getOwner().getProtocolManager().addPacketListener(new PacketAdapter(humbug.getOwner(), PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent e) {
                try {
                    final PacketContainer p = e.getPacket();
                    final StructureModifier<ItemStack> items = p.getItemModifier();
                    final ItemStack i = items.read(0);

                    if (i != null && isObfuscatable(i.getType())) {
                        Color color = null;

                        if (i.getItemMeta() instanceof LeatherArmorMeta) {
                            LeatherArmorMeta lam = (LeatherArmorMeta) i.getItemMeta();
                            color = lam.getColor();
                        }

                        final ItemStack is = new ItemStack(i.getType(), 1 , (short) 1);

                        if (i.getEnchantments().keySet().size() != 0) {
                            is.addEnchantment(Enchantment.DURABILITY, 1);
                        }

                        if (color != null) {
                            LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
                            lam.setColor(color);
                            is.setItemMeta(lam);
                        }

                        items.write(0, is);
                    }
                } catch (FieldAccessException exception) {
                    exception.printStackTrace();
                }
            }
        });

        //Strips potion duration length and sets it to 420 ticks so you can blaze it
        humbug.getOwner().getProtocolManager().addPacketListener(new PacketAdapter(humbug.getOwner(), PacketType.Play.Server.ENTITY_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent e) {
                try {
                    final PacketContainer p = e.getPacket();

                    if(e.getPlayer().getEntityId() != p.getIntegers().read(0)) {
                        p.getIntegers().write(1, 420);
                    }
                } catch (FieldAccessException exception) {
                    exception.printStackTrace();
                }
            }
        });

        //Make reported health random
        humbug.getOwner().getProtocolManager().addPacketListener(
                new PacketAdapter(humbug.getOwner(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA) {
                    public void onPacketSending(PacketEvent event) {
                        try {
                            final Player observer = event.getPlayer();

                            //Get the entity from the packet
                            final Entity entity = event.getPacket().getEntityModifier(observer.getWorld()).read(0);

                            event.setPacket(event.getPacket().deepClone());

                            //If the entity is not the observer, and the entity is alive, and the entity is not a dragon or wither,
                            //and the entity is not the observer's mount
                            if ((observer != entity) && ((entity instanceof LivingEntity)) &&
                                    (!(entity instanceof EnderDragon) && !(entity instanceof Wither)) && (entity.getPassenger() != observer)) {

                                final StructureModifier<List<WrappedWatchableObject>> watcher = event.getPacket().getWatchableCollectionModifier();

                                for (WrappedWatchableObject watch : watcher.read(0)) {
                                    if ((watch.getIndex() == 6) && ((Float) watch.getValue() > 0.0F)) {
                                        watch.setValue(20f);
                                    }
                                }
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void unload() {}

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
