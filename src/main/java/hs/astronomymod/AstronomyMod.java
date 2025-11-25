package hs.astronomymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstronomyMod implements ModInitializer {
    public static final String MOD_ID = "astronomymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Astronomy Items
    public static final Item PLANET_ITEM = new PlanetItem(new Item.Properties().stacksTo(1));
    public static final Item SUPERNOVA_ITEM = new SupernovaItem(new Item.Properties().stacksTo(1));
    public static final Item BLACKHOLE_ITEM = new BlackholeItem(new Item.Properties().stacksTo(1));
    public static final Item NEUTRON_STAR_ITEM = new NeutronStarItem(new Item.Properties().stacksTo(1));
    public static final Item PULSAR_ITEM = new PulsarItem(new Item.Properties().stacksTo(1));

    @Override
    public void onInitialize() {
        LOGGER.info("Astronomy Mod Initializing...");

        // Register items
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "planet"), PLANET_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "supernova"), SUPERNOVA_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "blackhole"), BLACKHOLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "neutron_star"), NEUTRON_STAR_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "pulsar"), PULSAR_ITEM);

        // Register player tick event for abilities
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(player -> {
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component != null) {
                    component.tick(player);
                }
            });
        });

        LOGGER.info("Astronomy Mod Initialized!");
    }
}