package hs.astronomymod.mixin;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
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
        if (!((Object) this instanceof InventoryScreen)) {
            return;
        }
        
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        int guiLeft = (screen.width - 176) / 2;
        int guiTop = (screen.height - 166) / 2;
        int slotX = guiLeft + 98;
        int slotY = guiTop + 44;

        if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
            mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
            
            AstronomySlotComponent comp = AstronomySlotComponent.getClient();
            ItemStack slotStack = comp.getAstronomyStack();
            ItemStack cursorStack = screen.getScreenHandler().getCursorStack();

            if (button == 0) {
                if (!cursorStack.isEmpty()) {
                    if (cursorStack.getItem() instanceof AstronomyItem) {
                        ItemStack oldStack = slotStack.copy();
                        comp.setAstronomyStack(cursorStack.copy());
                        screen.getScreenHandler().setCursorStack(oldStack);
                        astronomy$sendSlotUpdate(cursorStack.copy());
                        astronomy$playClickSound();
                    }
                } else {
                    if (!slotStack.isEmpty()) {
                        screen.getScreenHandler().setCursorStack(slotStack.copy());
                        comp.setAstronomyStack(ItemStack.EMPTY);
                        astronomy$sendSlotUpdate(ItemStack.EMPTY);
                        astronomy$playClickSound();
                    }
                }
            } else if (button == 1) {
                if (!cursorStack.isEmpty() && cursorStack.getItem() instanceof AstronomyItem && slotStack.isEmpty()) {
                    ItemStack singleItem = cursorStack.split(1);
                    comp.setAstronomyStack(singleItem);
                    astronomy$sendSlotUpdate(singleItem);
                    astronomy$playClickSound();
                }
            }
            cir.setReturnValue(true);
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.playSound(
                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F, 1.0F
            );
        }
    }
}
