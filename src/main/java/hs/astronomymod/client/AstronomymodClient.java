package hs.astronomymod.client;

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

        ModKeybindings.registerKeybindings();
        AstronomyPackets.registerS2CPackets();

        MinecraftClient client = MinecraftClient.getInstance();

        // HUD render event
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (client.player != null && !client.options.hudHidden) {
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

            while (ModKeybindings.ACTIVATE_ASTRONOMY_ABILITY.wasPressed()) {
                ClientPlayNetworking.send(new AstronomyPackets.ActivateAbilityPayload());
            }

            while (ModKeybindings.SELECT_ASTRONOMY_SLOT.wasPressed()) {
                astronomySlotScreen.setSelectedSlot(0);
            }
        });
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
