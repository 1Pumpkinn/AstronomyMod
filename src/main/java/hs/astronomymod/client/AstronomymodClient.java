package hs.astronomymod.client;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.client.screen.OverloadHud;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class AstronomymodClient implements ClientModInitializer {

    private static final OverloadHud overloadHud = new OverloadHud();

    @Override
    public void onInitializeClient() {
        AstronomyMod.LOGGER.info("Astronomy Mod Client Initializing...");

        ModKeybindings.registerKeybindings();

        // Register client-side packet handlers (but not the payload type - that's done in registerS2CPacketsServer)
        AstronomyPackets.registerS2CPacketsClient();

        MinecraftClient client = MinecraftClient.getInstance();

        // HUD render event
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.player != null && !client.options.hudHidden && client.currentScreen == null) {
                overloadHud.render(
                        context,
                        client.getWindow().getScaledWidth(),
                        client.getWindow().getScaledHeight(),
                        tickCounter
                );
            }
        });

        // Keybind tick event
        ClientTickEvents.END_CLIENT_TICK.register(c -> {

            // Activate primary ability keybind
            while (ModKeybindings.ACTIVATE_ASTRONOMY_ABILITY.wasPressed()) {
                if (ClientPlayNetworking.canSend(AstronomyPackets.ACTIVATE_ABILITY_ID)) {
                    ClientPlayNetworking.send(new AstronomyPackets.ActivateAbilityPayload(AbilityActivation.PRIMARY));
                    AstronomyMod.LOGGER.info("Sent primary ability packet");
                }
            }

            // Activate secondary ability keybind
            while (ModKeybindings.ACTIVATE_SECONDARY_ASTRONOMY_ABILITY.wasPressed()) {
                if (ClientPlayNetworking.canSend(AstronomyPackets.ACTIVATE_ABILITY_ID)) {
                    ClientPlayNetworking.send(new AstronomyPackets.ActivateAbilityPayload(AbilityActivation.SECONDARY));
                    AstronomyMod.LOGGER.info("Sent secondary ability packet");
                }
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
        // Slot selection removed - scroll input now unused.
    }
}