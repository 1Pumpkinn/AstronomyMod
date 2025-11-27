package hs.astronomymod.client;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.screen.AstronomySlotScreen;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class AstronomymodClient implements ClientModInitializer {

    private static final AstronomySlotScreen astronomySlotScreen = new AstronomySlotScreen();
    public static int scrollCooldown = 0;

    @Override
    public void onInitializeClient() {
        AstronomyMod.LOGGER.info("Astronomy Mod Client Initializing...");

        ModKeybindings.registerKeybindings();

        // Register client-side packet handlers (but not the payload type - that's done in registerS2CPacketsServer)
        AstronomyPackets.registerS2CPacketsClient();

        MinecraftClient client = MinecraftClient.getInstance();

        // HUD render event
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (client.player != null && !client.options.hudHidden && client.currentScreen == null) {
                astronomySlotScreen.render(
                        context,
                        client.getWindow().getScaledWidth(),
                        client.getWindow().getScaledHeight()
                );
            }
        });

        // Keybind tick event
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (scrollCooldown > 0) scrollCooldown--;

            // Activate ability keybind
            while (ModKeybindings.ACTIVATE_ASTRONOMY_ABILITY.wasPressed()) {
                if (ClientPlayNetworking.canSend(AstronomyPackets.ACTIVATE_ABILITY_ID)) {
                    ClientPlayNetworking.send(new AstronomyPackets.ActivateAbilityPayload());
                    AstronomyMod.LOGGER.info("Sent activate ability packet");
                }
            }

            // Select slot keybind
            while (ModKeybindings.SELECT_ASTRONOMY_SLOT.wasPressed()) {
                astronomySlotScreen.setSelectedSlot(0);
            }

            // Tick client component for client-side effects
            if (c.player != null) {
                AstronomySlotComponent.getClient().tickClient(c.player);
            }
        });

        AstronomyMod.LOGGER.info("Astronomy Mod Client Initialized!");
    }

    // Called by mixin
    public static void handleScrollInput(double yOffset) {
        if (scrollCooldown == 0 && yOffset != 0) {
            astronomySlotScreen.setSelectedSlot(0);
            scrollCooldown = 5;
        }
    }

    public static AstronomySlotScreen getAstronomySlotScreen() {
        return astronomySlotScreen;
    }
}