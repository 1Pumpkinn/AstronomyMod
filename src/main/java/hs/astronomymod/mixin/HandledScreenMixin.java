package hs.astronomymod.mixin;

import hs.astronomymod.item.AstronomyItem;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Unique
    private static final int SLOT_SIZE = 18;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof InventoryScreen)) return;
        double mouseX = click.x();
        double mouseY = click.y();
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        int guiLeft = (screen.width - 176) / 2;
        int guiTop = (screen.height - 166) / 2;
        int slotX = guiLeft + 76;
        int slotY = guiTop + 42;
        if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
            ItemStack cursorStack = screen.getScreenHandler().getCursorStack();
            if (!cursorStack.isEmpty() && !(cursorStack.getItem() instanceof AstronomyItem)) {
                cir.setReturnValue(true); // Cancel placement/drag
                return;
            }
            // else, allow standard logic
        }
    }
}