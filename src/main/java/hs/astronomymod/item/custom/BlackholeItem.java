package hs.astronomymod.item.custom;

import hs.astronomymod.abilities.blackhole.BlackholeAbility;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class BlackholeItem extends AstronomyItem {
    public BlackholeItem(Settings settings) {
        super(settings, new BlackholeAbility());
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§5Passive: Event Horizon"));
        tooltip.accept(Text.literal("  §7• Void Protection"));
        tooltip.accept(Text.literal("  §7• Massive Health Boost"));
        tooltip.accept(Text.literal("  §7• Item Magnetism"));
        tooltip.accept(Text.literal("§8Active: Singularity Collapse"));
        tooltip.accept(Text.literal("  §7• Extreme Gravity Well"));
        tooltip.accept(Text.literal("  §7• Crushing Damage"));
        tooltip.accept(Text.literal("§e+ Ethereal Form"));
        tooltip.accept(Text.literal("§e+ Enhanced Speed"));
    }
}