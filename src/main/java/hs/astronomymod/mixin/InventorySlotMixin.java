package hs.astronomymod.mixin;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventorySlotMixin extends HandledScreen<PlayerScreenHandler> {

    private static final Identifier ASTRONOMY_SLOT_TEXTURE =
            Identifier.of("astronomymod", "textures/gui/astronomy_slot.png");

    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {

        int slotX = x + backgroundWidth + 6;
        int slotY = y + 24;

        context.drawTexture(ASTRONOMY_SLOT_TEXTURE, slotX, slotY, 0, 0, 18, 18, 256, 256);

        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void clickAstronomySlot(double mouseX, double mouseY, int button, CallbackInfo ci) {

        int slotX = x + backgroundWidth + 6;
        int slotY = y + 24;

        if (mouseX >= slotX && mouseX < slotX + 18 &&
                mouseY >= slotY && mouseY < slotY + 18) {

            handleCustomSlotClick();
            ci.cancel();
        }
    }

    private void handleCustomSlotClick() {
        AstronomySlotComponent comp = AstronomySlotComponent.getClient();
        ItemStack slotStack = comp.getAstronomyStack();
        ItemStack cursor = handler.getCursorStack();

        if (!cursor.isEmpty() && cursor.getItem() instanceof AstronomyItem) {
            comp.setAstronomyStack(cursor.copy());
            handler.setCursorStack(slotStack);
        } else if (cursor.isEmpty() && !slotStack.isEmpty()) {
            handler.setCursorStack(slotStack);
            comp.setAstronomyStack(ItemStack.EMPTY);
        }
    }
}