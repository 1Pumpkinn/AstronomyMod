package hs.astronomymod.mixin;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import net.minecraft.client.gl.RenderPipelines;
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

@Mixin(InventoryScreen.class)
public abstract class InventorySlotMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private static final Identifier SLOT_TEXTURE =
            Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");

    @Unique
    private static final int SLOT_SIZE = 18;

    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int slotX = this.x + 77;
        int slotY = this.y + 44;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE,
                slotX, slotY,
                0.0F, 0.0F,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE
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

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int slotX = this.x + 77;
        int slotY = this.y + 44;

        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
            if (!stack.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
            } else {
                context.drawTooltip(
                        Text.literal("§6Astronomy Slot§r\n§7Place astronomy items here"),
                        mouseX, mouseY
                );
            }
        }
    }

    @Unique
    private boolean astronomy$isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}