package hs.astronomymod.client.screen;

import hs.astronomymod.client.AstronomySlotComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class OverloadHud {
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 8;

    public void render(DrawContext context, int screenWidth, int screenHeight, RenderTickCounter tickCounter) {
        AstronomySlotComponent component = AstronomySlotComponent.getClient();
        float current = component.getOverloadLevel();
        float max = AstronomySlotComponent.getMaxOverload();

        if (max <= 0f) return;

        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = screenHeight - 52;

        int backgroundColor = 0x90000000;
        int borderColor = 0xFFFFFFFF;
        int fillColor = current >= max ? 0xFFAA0000 : 0xFFFCC342;

        context.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, borderColor);
        context.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, backgroundColor);

        int filledWidth = (int) (BAR_WIDTH * (current / max));
        if (filledWidth > 0) {
            context.fill(x, y, x + filledWidth, y + BAR_HEIGHT, fillColor);
        }

        Text label = Text.translatable("hud.astronomymod.overload");
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(label);
        context.drawText(
                textRenderer,
                label,
                x + (BAR_WIDTH - textWidth) / 2,
                y - 10,
                0xFFFFFF,
                false
        );
    }
}

