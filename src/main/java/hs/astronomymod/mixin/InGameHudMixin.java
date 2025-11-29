package hs.astronomymod.mixin;

import hs.astronomymod.effect.ModStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    
    @Shadow
    private MinecraftClient client;

    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void astronomymod$hideHeart(DrawContext context, Object type, int x, int y, int v, boolean blinking, boolean halfHeart, CallbackInfo ci) {
        // Hide all hearts when player has HiddenHeartsStatusEffect
        if (!blinking && client.player != null && 
            client.player.hasStatusEffect(ModStatusEffects.getHiddenHeartsEntry())) {
            // Cancel heart rendering to hide it
            ci.cancel();
        }
    }

    @Inject(method = "renderFoodLevel", at = @At("HEAD"), cancellable = true)
    private void astronomymod$hideFoodLevel(DrawContext context, int x, int y, CallbackInfo ci) {
        if (client.player != null && 
            client.player.hasStatusEffect(ModStatusEffects.getHiddenHeartsEntry())) {
            // Cancel food level rendering to hide it
            ci.cancel();
        }
    }
}

