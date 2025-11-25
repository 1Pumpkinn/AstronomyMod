package hs.astronomymod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.function.Consumer;

public abstract class AstronomyItem extends Item {
    public AstronomyItem(Settings settings) {
        super(settings);
    }

    public abstract void applyPassiveAbility(ServerPlayerEntity player);

    public abstract void applyActiveAbility(ServerPlayerEntity player);

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        tooltip.accept(Text.literal("§6Astronomy Item"));
        tooltip.accept(Text.literal("§7Place in Astronomy Slot for abilities"));
        addCustomTooltip(tooltip);
    }

    protected abstract void addCustomTooltip(Consumer<Text> tooltip);
}