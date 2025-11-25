package hs.astronomymod.item;

import hs.astronomymod.*;
import hs.astronomymod.item.custom.*;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    // Astronomy items
    public static final Item PLANET_ITEM =
            registerItem("planet", PlanetItem::new, new Item.Settings().maxCount(1));

    public static final Item SUPERNOVA_ITEM =
            registerItem("supernova", SupernovaItem::new, new Item.Settings().maxCount(1));

    public static final Item BLACKHOLE_ITEM =
            registerItem("blackhole", BlackholeItem::new, new Item.Settings().maxCount(1));

    public static final Item NEUTRON_STAR_ITEM =
            registerItem("neutron_star", NeutronStarItem::new, new Item.Settings().maxCount(1));

    public static final Item PULSAR_ITEM =
            registerItem("pulsar", PulsarItem::new, new Item.Settings().maxCount(1));


    /**
     * Generic register helper identical to ElementMod
     */
    public static <I extends Item> I registerItem(String name, Function<Item.Settings, I> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AstronomyMod.MOD_ID, name));
        I item = factory.apply(settings.registryKey(key));

        // BlockItem handling (for future blocks)
        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem);
        }

        return Registry.register(Registries.ITEM, key, item);
    }

    public static Item registerItem(String name, Function<Item.Settings, Item> factory) {
        return registerItem(name, factory, new Item.Settings());
    }

    /**
     * Call from onInitialize()
     */
    public static void registerModItems() {
        AstronomyMod.LOGGER.info("Astronomy mod items registered");
    }
}
