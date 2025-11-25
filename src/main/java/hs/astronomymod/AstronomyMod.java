package hs.astronomymod;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.command.AstronomyCommands;
import hs.astronomymod.item.ModItems;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstronomyMod implements ModInitializer {
    public static final String MOD_ID = "astronomymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Astronomy Mod Initializing...");
        ModItems.registerModItems();
        AstronomyCommands.register();

        // Register C2S packets
        AstronomyPackets.registerC2SPackets();

        // Tick event for abilities
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                AstronomySlotComponent comp = AstronomySlotComponent.get(player);
                if (comp != null) comp.tickServer(player);
            }
        });

        LOGGER.info("Astronomy Mod Initialized!");
    }
}