package hs.astronomymod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Properties;

public abstract class AstronomyItem extends Item {
    public AstronomyItem(Properties properties) {
        super(properties);
    }

    public abstract void applyPassiveAbility(net.minecraft.server.level.ServerPlayer player);

    public abstract void applyActiveAbility(net.minecraft.server.level.ServerPlayer player);

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6Astronomy Item"));
        tooltipComponents.add(Component.literal("§7Place in Astronomy Slot for abilities"));
        addCustomTooltip(tooltipComponents);
    }

    protected abstract void addCustomTooltip(List<Component> tooltipComponents);
}
