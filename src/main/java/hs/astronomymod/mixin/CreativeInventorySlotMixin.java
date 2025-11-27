package hs.astronomymod.mixin;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import hs.astronomymod.network.AstronomyPackets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventorySlotMixin {

    @Unique
    private static final Identifier SLOT_TEXTURE =
            Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");

    @Unique
    private static final int SLOT_SIZE = 18;

    // --------------------------
    //      RENDER CUSTOM SLOT
    // --------------------------
    @Inject(method = "render", at = @At("TAIL"))
    private void renderAstronomySlot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;

        // Only draw slot on the SURVIVAL INVENTORY creative tab
        if (CreativeInventoryScreenAccessor.getSelectedTab().getType() != ItemGroup.Type.INVENTORY) return;

        int guiLeft = (screen.width - 195) / 2;
        int guiTop = (screen.height - 136) / 2;

        // Matches SURVIVAL INVENTORY layout
        int slotX = guiLeft + 127;
        int slotY = guiTop + 20;

        // Draw slot background
        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE,
                slotX, slotY,
                0f, 0f,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE,
                -1
        );

        // Draw item inside slot
        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
            context.drawStackOverlay(
                    MinecraftClient.getInstance().textRenderer,
                    stack,
                    slotX + 1, slotY + 1
            );
        }

        // Hover highlight + tooltip
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x80FFFFFF);

            if (!stack.isEmpty()) {
                context.drawItemTooltip(
                        MinecraftClient.getInstance().textRenderer,
                        stack,
                        mouseX, mouseY
                );
            } else {
                context.drawTooltip(
                        MinecraftClient.getInstance().textRenderer,
                        Text.literal("§6Astronomy Slot\n§7Place astronomy items here"),
                        mouseX, mouseY
                );
            }
        }
    }

    // --------------------------
    //      CLICK HANDLING
    // --------------------------
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;

        // Only handle slot on SURVIVAL INVENTORY tab
        if (CreativeInventoryScreenAccessor.getSelectedTab().getType() != ItemGroup.Type.INVENTORY) return;

        double mouseX = click.x();
        double mouseY = click.y();

        int guiLeft = (screen.width - 195) / 2;
        int guiTop = (screen.height - 136) / 2;

        int slotX = guiLeft + 127;
        int slotY = guiTop + 20;

        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            if (astronomy$handleAstronomySlotClick(click, screen)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean astronomy$handleAstronomySlotClick(Click click, CreativeInventoryScreen screen) {
        if (click.button() != 0) return false; // Only left click

        ItemStack cursorStack = screen.getScreenHandler().getCursorStack();
        ItemStack slotStack = AstronomySlotComponent.getClient().getAstronomyStack();

        // Block non-astronomy items
        if (!cursorStack.isEmpty() && !(cursorStack.getItem() instanceof AstronomyItem)) {
            return true;
        }

        // Take item from slot (creative: just clears the slot, doesn't give item back)
        if (cursorStack.isEmpty() && !slotStack.isEmpty()) {
            if (ClientPlayNetworking.canSend(AstronomyPackets.TAKE_FROM_SLOT_CREATIVE_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.TakeFromSlotCreativePayload());
            }
        }
        // Place item in empty slot (creative: copies item from cursor, leaves cursor unchanged)
        else if (!cursorStack.isEmpty() && slotStack.isEmpty()) {
            ItemStack toPlace = cursorStack.copy();
            toPlace.setCount(1);

            if (ClientPlayNetworking.canSend(AstronomyPackets.PLACE_IN_SLOT_CREATIVE_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.PlaceInSlotCreativePayload(toPlace));
            }
            // Don't modify cursor - this is intentional for creative mode
        }
        // Swap items (creative: replaces slot item, cursor stays the same)
        else if (!cursorStack.isEmpty() && !slotStack.isEmpty()) {
            ItemStack toPlace = cursorStack.copy();
            toPlace.setCount(1);

            if (ClientPlayNetworking.canSend(AstronomyPackets.SWAP_SLOT_CREATIVE_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.SwapSlotCreativePayload(toPlace));
            }
            // Don't modify cursor - this is intentional for creative mode
        }

        return true;
    }

    @Unique
    private boolean astronomy$isMouseOverSlot(double mouseX, double mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}