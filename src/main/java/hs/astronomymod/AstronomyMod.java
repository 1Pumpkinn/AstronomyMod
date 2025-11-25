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

    // Astronomy Items
    public static final Item PLANET_ITEM = new PlanetItem(new Item.Settings().maxCount(1));
    public static final Item SUPERNOVA_ITEM = new SupernovaItem(new Item.Settings().maxCount(1));
    public static final Item BLACKHOLE_ITEM = new BlackholeItem(new Item.Settings().maxCount(1));
    public static final Item NEUTRON_STAR_ITEM = new NeutronStarItem(new Item.Settings().maxCount(1));
    public static final Item PULSAR_ITEM = new PulsarItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        LOGGER.info("Astronomy Mod Initializing...");

        // Register items
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "planet"), PLANET_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "supernova"), SUPERNOVA_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "blackhole"), BLACKHOLE_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "neutron_star"), NEUTRON_STAR_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "pulsar"), PULSAR_ITEM);

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