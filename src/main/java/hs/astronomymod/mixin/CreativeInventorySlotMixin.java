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
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventorySlotMixin {
    @Unique
    private static final Identifier SLOT_TEXTURE = Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");
    @Unique
    private static final int SLOT_SIZE = 18;

    @Shadow
    private static int selectedTab;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderAstronomySlot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Only render on the inventory tab (tab index 12 in creative)
        if (selectedTab != 12) return; // 12 is the survival inventory tab

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        int screenWidth = ((net.minecraft.client.gui.screen.Screen) screen).width;
        int screenHeight = ((net.minecraft.client.gui.screen.Screen) screen).height;

        // Position calculation for creative inventory
        int guiLeft = (screenWidth - 195) / 2;
        int guiTop = (screenHeight - 136) / 2;
        int slotX = guiLeft + 127;
        int slotY = guiTop + 20;

        // Draw slot background
        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE, slotX, slotY,
                0f, 0f,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, -1
        );

        // Draw item in slot
        ItemStack stack = AstronomySlotComponent.getClient().getAstronomyStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, slotX + 1, slotY + 1);
            context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, stack, slotX + 1, slotY + 1);
        }

        // Draw hover highlight
        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            context.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x80FFFFFF);

            // Draw tooltip
            if (!stack.isEmpty()) {
                context.drawItemTooltip(MinecraftClient.getInstance().textRenderer, stack, mouseX, mouseY);
            } else {
                context.drawTooltip(MinecraftClient.getInstance().textRenderer,
                        Text.literal("§6Astronomy Slot§r\n§7Place astronomy items here"),
                        mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        // Only handle clicks on the survival inventory tab
        if (selectedTab != 12) return;

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        double mouseX = click.x();
        double mouseY = click.y();

        int screenWidth = ((net.minecraft.client.gui.screen.Screen) screen).width;
        int screenHeight = ((net.minecraft.client.gui.screen.Screen) screen).height;

        int guiLeft = (screenWidth - 195) / 2;
        int guiTop = (screenHeight - 136) / 2;
        int slotX = guiLeft + 127;
        int slotY = guiTop + 20;

        if (astronomy$isMouseOverSlot(mouseX, mouseY, slotX, slotY)) {
            if (astronomy$handleAstronomySlotClick(click)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean astronomy$handleAstronomySlotClick(Click click) {
        if (click.button() != 0) return false; // Only left click

        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        ItemStack cursorStack = ((net.minecraft.client.gui.screen.ingame.HandledScreen<?>) screen).getScreenHandler().getCursorStack();
        ItemStack slotStack = AstronomySlotComponent.getClient().getAstronomyStack();

        // Validate cursor stack
        if (!cursorStack.isEmpty() && !(cursorStack.getItem() instanceof AstronomyItem)) {
            return true; // Reject non-astronomy items
        }

        // Handle the swap
        if (cursorStack.isEmpty() && !slotStack.isEmpty()) {
            // Taking item from slot
            if (ClientPlayNetworking.canSend(AstronomyPackets.TAKE_FROM_SLOT_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.TakeFromSlotPayload());
            }
        } else if (!cursorStack.isEmpty() && slotStack.isEmpty()) {
            // Placing item in slot
            ItemStack toPlace = cursorStack.copy();
            toPlace.setCount(1);

            if (ClientPlayNetworking.canSend(AstronomyPackets.PLACE_IN_SLOT_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.PlaceInSlotPayload(toPlace));
            }
        } else if (!cursorStack.isEmpty() && !slotStack.isEmpty()) {
            // Swapping items
            if (cursorStack.getCount() > 1) {
                return true; // Can't swap with stacks
            }

            ItemStack toPlace = cursorStack.copy();
            if (ClientPlayNetworking.canSend(AstronomyPackets.SWAP_SLOT_ID)) {
                ClientPlayNetworking.send(new AstronomyPackets.SwapSlotPayload(toPlace));
            }
        }

        return true;
    }

    @Unique
    private boolean astronomy$isMouseOverSlot(double mouseX, double mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}