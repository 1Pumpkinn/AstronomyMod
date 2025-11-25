package hs.astronomymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstronomyMod implements ModInitializer {
    public static final String MOD_ID = "astronomymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Declare items
    public static Item PLANET_ITEM;
    public static Item SUPERNOVA_ITEM;
    public static Item BLACKHOLE_ITEM;
    public static Item NEUTRON_STAR_ITEM;
    public static Item PULSAR_ITEM;

    // Static block to register items before onInitialize is called
    static {
        PLANET_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "planet"),
                new PlanetItem(new Item.Settings().maxCount(1)));
        SUPERNOVA_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "supernova"),
                new SupernovaItem(new Item.Settings().maxCount(1)));
        BLACKHOLE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "blackhole"),
                new BlackholeItem(new Item.Settings().maxCount(1)));
        NEUTRON_STAR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "neutron_star"),
                new NeutronStarItem(new Item.Settings().maxCount(1)));
        PULSAR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "pulsar"),
                new PulsarItem(new Item.Settings().maxCount(1)));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Astronomy Mod Initializing...");

        // Register player tick event for abilities
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component != null) {
                    component.tick(player);
                }
            });
        });

        LOGGER.info("Astronomy Mod Initialized!");
    }
}