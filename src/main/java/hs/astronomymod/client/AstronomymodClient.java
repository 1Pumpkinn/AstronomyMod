package hs.astronomymod.client;

import hs.astronomymod.client.screen.AstronomySlotScreen;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

public class AstronomymodClient implements ClientModInitializer {

    private static final AstronomySlotScreen astronomySlotScreen = new AstronomySlotScreen();
    private static int scrollCooldown = 0;

    @Override
    public void onInitializeClient() {
        ModKeybindings.registerKeybindings();
        AstronomyPackets.registerS2CPackets();

        MinecraftClient client = MinecraftClient.getInstance();

        // Register HUD rendering
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (client.player != null && !client.options.hudHidden) {
                astronomySlotScreen.render(context, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            }
        });

        // Register client tick events for keybindings
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (scrollCooldown > 0) scrollCooldown--;

            // Activate ability keybind
            while (ModKeybindings.ACTIVATE_ASTRONOMY_ABILITY.wasPressed()) {
                ClientPlayNetworking.send(new AstronomyPackets.ActivateAbilityPayload());
            }

            // Select slot keybind
            while (ModKeybindings.SELECT_ASTRONOMY_SLOT.wasPressed()) {
                astronomySlotScreen.setSelectedSlot(0);
            }
        });

        // Register a GLFW scroll callback
        long windowHandle = client.getWindow().getHandle();
        GLFW.glfwSetScrollCallback(windowHandle, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                if (scrollCooldown == 0) {
                    if (yOffset != 0) {
                        astronomySlotScreen.setSelectedSlot(0); // example: change slot
                        scrollCooldown = 5;
                    }
                }
            }
        });
    }

    public static AstronomySlotScreen getAstronomySlotScreen() {
        return astronomySlotScreen;
    }
}
