package hs.astronomymod.client.screen;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.AstronomyMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AstronomySlotScreen {
    private static final Identifier SLOT_TEXTURE = Identifier.of(AstronomyMod.MOD_ID, "textures/gui/astronomy_slot.png");
    private static final int SLOT_SIZE = 22;
    private static final int SLOT_PADDING = 2;

    private int selectedSlot = 0;

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        int x = screenWidth / 2 - 91 - SLOT_SIZE - 4;
        int y = screenHeight - 23;
        // Draw slot background
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                SLOT_TEXTURE,
                x, y,
                0f, 0f,
                SLOT_SIZE, SLOT_SIZE,
                SLOT_SIZE, SLOT_SIZE,
                256, 256,
                0xFFFFFFFF
        );

        // Draw selection highlight if selected
        if (selectedSlot == 0) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    SLOT_TEXTURE,
                    x, y,
                    SLOT_SIZE, 0f,
                    SLOT_SIZE, SLOT_SIZE,
                    SLOT_SIZE, SLOT_SIZE,
                    256, 256,
                    0xFFFFFFFF
            );
        }

        // Draw item in slot
        AstronomySlotComponent component = AstronomySlotComponent.CLIENT_INSTANCE;
        if (component != null) {
            ItemStack stack = component.getAstronomyStack();
            if (!stack.isEmpty()) {
                context.drawItem(stack, x + 3, y + 3);
            }
        }
    }

    public void setSelectedSlot(int slot) {
        this.selectedSlot = slot;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }
}
