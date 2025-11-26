package hs.astronomymod.mixin;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryScreen.class, priority = 1001)
public abstract class InventorySlotMixin extends HandledScreen<PlayerScreenHandler> {
    @Unique private static final Identifier SLOT_TEXTURE = Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");
    @Unique private static final int SLOT_SIZE = 18;
    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); }

    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int slotX = this.x + 76;
        int slotY = this.y + 42;
        context.drawTexture(
            net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
            SLOT_TEXTURE, slotX, slotY,
            0f, 0f,
            SLOT_SIZE, SLOT_SIZE,
            SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, -1
        );
        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
            context.drawStackOverlay(this.textRenderer, stack, slotX + 1, slotY + 1);
        }
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x80FFFFFF);
        }
    }

    // Tooltip rendering (optional)
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int slotX = this.x + 76, slotY = this.y + 42;
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
            if (!stack.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
            } else {
                context.drawTooltip(this.textRenderer, Text.literal("§6Astronomy Slot§r\n§7Place astronomy items here"), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        int slotX = this.x + 76, slotY = this.y + 42;
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            ItemStack cursor = this.handler.getCursorStack();
            if (!cursor.isEmpty() && !(cursor.getItem() instanceof hs.astronomymod.item.AstronomyItem)) {
                // Invalid: non-Astronomy item, REJECT
                return true;
            }
            // else, allow usual placement/take
        }
        return super.mouseClicked(click, doubled);
    }

    @Unique
    private boolean astronomy$isMouseOverSlot(double mouseX, double mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}