package hs.astronomymod.mixin;

import hs.astronomymod.effect.ModStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow private MinecraftClient client;

    // Hide health hearts
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void astronomymod$hideHearts(
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
        if (player != null && player.hasStatusEffect(ModStatusEffects.getHiddenHeartsEntry())) {
            ci.cancel();
        }
    }

    // Hide food bar
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void astronomymod$hideFood(
            DrawContext context,
            PlayerEntity player,
            int top,
            int right,
            CallbackInfo ci
    ) {
        if (player != null && player.hasStatusEffect(ModStatusEffects.getHiddenHeartsEntry())) {
            ci.cancel();
        }
    }
}