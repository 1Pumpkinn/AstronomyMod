package hs.astronomymod.mixin;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventorySlotMixin extends HandledScreen<PlayerScreenHandler> {

    private static final Identifier ASTRONOMY_SLOT_TEXTURE =
            Identifier.of("astronomymod", "textures/gui/astronomy_slot.png");

    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {

        int slotX = x + 176;
        int slotY = y + 8;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, ASTRONOMY_SLOT_TEXTURE, slotX, slotY, 0f, 0f, 18, 18, 18, 18, 256, 256, 0xFFFFFFFF);

        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
        }
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        int slotX = x + 176;
        int slotY = y + 8;

        if (click.x() >= slotX && click.x() < slotX + 18 &&
                click.y() >= slotY && click.y() < slotY + 18) {

            handleCustomSlotClick();
            return true;
        }

        return super.mouseClicked(click, doubled);
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