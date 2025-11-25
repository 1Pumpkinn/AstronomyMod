package hs.astronomymod.mixin;

import hs.astronomymod.client.AstronomymodClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void handleScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

        if (MinecraftClient.getInstance().player == null)
            return;

        AstronomymodClient.handleScrollInput(vertical);
    }
}
