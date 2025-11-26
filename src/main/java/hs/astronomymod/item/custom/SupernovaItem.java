package hs.astronomymod.item.custom;

import hs.astronomymod.abilities.supernova.SupernovaAbility;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SupernovaItem extends AstronomyItem {
    public SupernovaItem(Settings settings) {
        super(settings, new SupernovaAbility());
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§cPassive: Stellar Heat"));
        tooltip.accept(Text.literal("  §7• Fire Immunity"));
        tooltip.accept(Text.literal("  §7• Burning Aura"));
        tooltip.accept(Text.literal("  §7• Night Vision"));
        tooltip.accept(Text.literal("§6Active: Supernova Explosion"));
        tooltip.accept(Text.literal("  §7• Massive Damage & Knockback"));
        tooltip.accept(Text.literal("§e+ Speed & Strength Boost"));
        tooltip.accept(Text.literal("§e+ Resistance"));
    }
}