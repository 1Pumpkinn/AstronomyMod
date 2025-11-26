package hs.astronomymod.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
    private static final int SLOT_SIZE = 20;

    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        // Position aligned with offhand slot (same Y level)
        int slotX = this.x + 77;
        int slotY = this.y + 65;

        // Draw slot background using the correct method signature
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE,
                slotX, slotY,
                0.0F, 0.0F,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE
        );

        // Draw the item in the slot
        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
            context.drawStackOverlay(this.textRenderer, stack, slotX + 1, slotY + 1);
        }

        // Draw hover highlight
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x80FFFFFF);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int slotX = this.x + 77;
        int slotY = this.y + 65;

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

    // Public method that can be called from the event handler
    @Unique
    public boolean astronomy$handleSlotClick(double mouseX, double mouseY, int button) {
        int slotX = this.x + 77;
        int slotY = this.y + 65;

        if (astronomy$isMouseOverSlot((int)mouseX, (int)mouseY, slotX, slotY)) {
            astronomy$handleSlotClickInternal(button);
            return true;
        }
        return false;
    }

    @Unique
    public boolean astronomy$isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    @Unique
    private void astronomy$handleSlotClickInternal(int button) {
        AstronomySlotComponent comp = AstronomySlotComponent.getClient();
        ItemStack slotStack = comp.getAstronomyStack();
        ItemStack cursorStack = this.handler.getCursorStack();

        if (button == 0) { // Left click
            if (!cursorStack.isEmpty()) {
                // Try to place item from cursor into slot
                if (cursorStack.getItem() instanceof AstronomyItem) {
                    ItemStack oldStack = slotStack.copy();
                    comp.setAstronomyStack(cursorStack.copy());
                    this.handler.setCursorStack(oldStack);
                    astronomy$sendSlotUpdate(cursorStack.copy());
                    astronomy$playClickSound();
                }
            } else {
                // Pick up item from slot
                if (!slotStack.isEmpty()) {
                    this.handler.setCursorStack(slotStack.copy());
                    comp.setAstronomyStack(ItemStack.EMPTY);
                    astronomy$sendSlotUpdate(ItemStack.EMPTY);
                    astronomy$playClickSound();
                }
            }
        } else if (button == 1) { // Right click
            if (!cursorStack.isEmpty() && cursorStack.getItem() instanceof AstronomyItem && slotStack.isEmpty()) {
                ItemStack singleItem = cursorStack.split(1);
                comp.setAstronomyStack(singleItem);
                astronomy$sendSlotUpdate(singleItem);
                astronomy$playClickSound();
            }
        }
    }

    @Unique
    private void astronomy$sendSlotUpdate(ItemStack stack) {
        if (ClientPlayNetworking.canSend(AstronomyPackets.UPDATE_SLOT_ID)) {
            ClientPlayNetworking.send(new AstronomyPackets.UpdateSlotPayload(stack));
        }
    }

    @Unique
    private void astronomy$playClickSound() {
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(
                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F
            );
        }
    }
}