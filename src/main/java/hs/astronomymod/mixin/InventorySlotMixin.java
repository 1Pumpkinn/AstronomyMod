package hs.astronomymod.mixin;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import net.minecraft.client.gl.RenderPipelines;
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

    @Unique
    private static final Identifier SLOT_TEXTURE =
            Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");

    @Unique
    private static final int SLOT_SIZE = 18;

    public InventorySlotMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at = @At("TAIL"))
    private void drawAstronomySlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int slotX = this.x + 76;
        int slotY = this.y + 42;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE,
                slotX,
                slotY,
                0f, 0f,
                SLOT_SIZE,
                SLOT_SIZE,
                SLOT_SIZE,
                SLOT_SIZE,
                SLOT_SIZE,
                SLOT_SIZE,
                -1
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

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int slotX = this.x + 76;
        int slotY = this.y + 42;

        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
            if (!stack.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
            } else {
                context.drawTooltip(
                        this.textRenderer,
                        Text.literal("§6Astronomy Slot§r\n§7Place astronomy items here"),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Convert click coordinates to screen coordinates
        int slotX = this.x + 76;
        int slotY = this.y + 42;

        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            if (button == 0 || button == 1) {
                astronomy$handleSlotClick(button);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Unique
    private void astronomy$handleSlotClick(int button) {
        ItemStack cursorStack = this.handler.getCursorStack();
        ItemStack slotStack = AstronomySlotComponent.getClient().getAstronomyStack();

        if (button == 0) { // Left click
            if (!cursorStack.isEmpty() && slotStack.isEmpty()) {
                AstronomySlotComponent.getClient().setAstronomyStack(cursorStack.copy());
                this.handler.setCursorStack(ItemStack.EMPTY);
            } else if (cursorStack.isEmpty() && !slotStack.isEmpty()) {
                this.handler.setCursorStack(slotStack.copy());
                AstronomySlotComponent.getClient().setAstronomyStack(ItemStack.EMPTY);
            } else if (!cursorStack.isEmpty() && !slotStack.isEmpty()) {
                ItemStack temp = cursorStack.copy();
                this.handler.setCursorStack(slotStack.copy());
                AstronomySlotComponent.getClient().setAstronomyStack(temp);
            }
        } else if (button == 1) { // Right click
            if (!cursorStack.isEmpty() && slotStack.isEmpty()) {
                ItemStack singleItem = cursorStack.copy();
                singleItem.setCount(1);
                AstronomySlotComponent.getClient().setAstronomyStack(singleItem);
                cursorStack.decrement(1);
            } else if (cursorStack.isEmpty() && !slotStack.isEmpty()) {
                this.handler.setCursorStack(slotStack.copy());
                AstronomySlotComponent.getClient().setAstronomyStack(ItemStack.EMPTY);
            }
        }
    }

    @Unique
    private boolean astronomy$isMouseOverSlot(double mouseX, double mouseY, int slotX, int slotY) {
        // The click coordinates are already in screen space, so we need to compare directly
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}