package hs.astronomymod.client.screen;

import hs.astronomymod.effect.ModStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public class HiddenHeartsHud {
    
    public static boolean shouldHideBars() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        
        var hiddenHeartsEntry = ModStatusEffects.getHiddenHeartsEntry();
        if (hiddenHeartsEntry == null) return false;
        
        return client.player.getStatusEffects().stream().anyMatch(effect -> 
            effect.getEffectType().equals(hiddenHeartsEntry));
    }
    
    // This will be used to overlay and hide the health/food bars
    public static void renderOverlay(DrawContext context, int screenWidth, int screenHeight) {
        if (!shouldHideBars()) return;
        
        // Draw a black overlay over the health and food bar areas to hide them
        // Health bar is typically at: left side, around y = screenHeight - 39
        // Food bar is typically at: left side, around y = screenHeight - 29
        
        int healthBarY = screenHeight - 39;
        int foodBarY = screenHeight - 29;
        int barWidth = 91; // Standard health/food bar width
        int barHeight = 9; // Standard bar height
        
        // Hide health bar
        context.fill(10, healthBarY, 10 + barWidth, healthBarY + barHeight, 0xFF000000);
        
        // Hide food bar
        context.fill(10, foodBarY, 10 + barWidth, foodBarY + barHeight, 0xFF000000);
    }
}

