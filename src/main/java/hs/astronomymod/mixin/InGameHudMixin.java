package hs.astronomymod.mixin;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.effect.ModStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;
    
    @Unique
    private static final Identifier CUSTOM_HEART_TEXTURE = Identifier.of(AstronomyMod.MOD_ID, "textures/gui/custom_hearts.png");
    @Unique
    private static final Identifier GUI_ICONS = Identifier.of("minecraft", "textures/gui/icons.png");

    private static boolean hasLoggedOnce = false;

    // Try injecting into render method and log to see if it works
    @Inject(method = "render", at = @At("HEAD"))
    private void astronomymod$onRender(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!hasLoggedOnce) {
            hasLoggedOnce = true;
        }
    }

    // Method 1: Change health bar texture
    @Inject(
            method = "renderHealthBar",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void astronomymod$changeHealthBarTexture(
            DrawContext context,
            PlayerEntity player,
            int x,
            int y,
            int lines,
            int regeneratingHeartIndex,
            float maxHealth,
            int lastHealth,
            int health,
            int absorption,
            boolean blinking,
            CallbackInfo ci
    ) {
        // Only change texture if this is the client player and they have the effect
        if (this.client != null && this.client.player != null && 
            player == this.client.player && shouldChangeTexture(player)) {
            ci.cancel();
            renderCustomHealthBar(context, player, x, y, lines, regeneratingHeartIndex, 
                    maxHealth, lastHealth, health, absorption, blinking);
        }
    }


    @Unique
    private void renderCustomHealthBar(DrawContext context, PlayerEntity player, int x, int y, 
            int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, 
            int health, int absorption, boolean blinking) {
        // Render health bar with custom texture
        // When hidden hearts effect is active, always show full hearts regardless of actual health
        int heartSize = 9;
        int heartsPerRow = 10;
        int totalHearts = (int)Math.ceil(maxHealth / 2.0);
        int textureWidth = 256; // Standard GUI texture width
        int textureHeight = 256; // Standard GUI texture height
        
        for (int i = 0; i < totalHearts && i < lines * heartsPerRow; i++) {
            int row = i / heartsPerRow;
            int col = i % heartsPerRow;
            int heartX = x + col * heartSize;
            int heartY = y - row * (heartSize + 1);
            
            // Always show full hearts when hidden hearts effect is active
            // This makes it so the player can't see their actual health
            float u = 0.0f; // Always full heart
            float v = 0.0f;
            
            context.drawTexture(
                    net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                    CUSTOM_HEART_TEXTURE, heartX, heartY,
                    u, v,
                    heartSize, heartSize,
                    heartSize, heartSize, textureWidth, textureHeight,
                    0xFFFFFFFF // No blinking effect either
            );
        }
        
        // Don't render absorption hearts when hidden hearts effect is active
        // This keeps the deception consistent
    }


    private boolean shouldChangeTexture(PlayerEntity player) {
        if (player == null) {
            return false;
        }

        try {
            RegistryEntry<StatusEffect> hiddenHeartsEntry = ModStatusEffects.getHiddenHeartsEntry();

            if (hiddenHeartsEntry == null) {
                return false;
            }

            // Check if the player has the effect by checking their status effects
            // Use value() to get the actual StatusEffect instance for comparison
            StatusEffect hiddenHeartsEffect = hiddenHeartsEntry.value();
            if (hiddenHeartsEffect == null) {
                return false;
            }

            // Check if the player has this specific effect
            boolean hasEffect = player.getStatusEffects().stream()
                    .anyMatch(effectInstance -> {
                        StatusEffect effect = effectInstance.getEffectType().value();
                        return effect != null && effect == hiddenHeartsEffect;
                    });

            return hasEffect;
        } catch (Exception e) {
            return false;
        }
    }
}