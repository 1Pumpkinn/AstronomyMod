package hs.astronomymod.item.custom;

import hs.astronomymod.abilities.neutronstar.NeutronStarAbility;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class NeutronStarItem extends AstronomyItem {
    public NeutronStarItem(Settings settings) {
        super(settings, new NeutronStarAbility());
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§7Passive: Ultra-Dense Core"));
        tooltip.accept(Text.literal("  §7• Extreme Resistance"));
        tooltip.accept(Text.literal("  §7• Massive Strength"));
        tooltip.accept(Text.literal("  §7• Knockback Immunity"));
        tooltip.accept(Text.literal("§fActive: Magnetic Storm"));
        tooltip.accept(Text.literal("  §7• Complete Immobilization"));
        tooltip.accept(Text.literal("  §7• Massive Repulsion"));
        tooltip.accept(Text.literal("§e+ Unstoppable Force"));
        tooltip.accept(Text.literal("§e+ Enhanced Mining"));
    }
}
