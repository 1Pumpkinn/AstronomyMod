package hs.astronomymod;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.command.AstronomyCommands;
import hs.astronomymod.item.ModItems;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
                if (comp != null) {
                    comp.tickServer(player);
                }
            }
        });

        // Sync astronomy slot when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            AstronomySlotComponent component = AstronomySlotComponent.get(player);

            // Send current slot state to client
            sender.sendPacket(new AstronomyPackets.SyncSlotPayload(component.getAstronomyStack()));
            LOGGER.info("Synced astronomy slot for player: {}", player.getName().getString());
        });

        // Clean up player data on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            AstronomySlotComponent.remove(handler.getPlayer().getUuid());
            LOGGER.info("Cleaned up astronomy data for player: {}", handler.getPlayer().getName().getString());
        });

        LOGGER.info("Astronomy Mod Initialized!");
    }
}