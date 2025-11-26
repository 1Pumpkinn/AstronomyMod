package hs.astronomymod.item.custom;

import hs.astronomymod.abilities.planet.PlanetAbility;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PlanetItem extends AstronomyItem {

    public PlanetItem(Settings settings) {
        super(settings, new PlanetAbility());
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§bPassive: Orbital Stability"));
        tooltip.accept(Text.literal("  §7• Slow Falling & Step Assist"));
        tooltip.accept(Text.literal("  §7• Periodic Regeneration"));
        tooltip.accept(Text.literal("§aActive: Gravitational Singularity"));
        tooltip.accept(Text.literal("  §7• Mass Pull & Levitation"));
        tooltip.accept(Text.literal("§e+ Enhanced Resistance"));
        tooltip.accept(Text.literal("§e+ Absorption Shield"));
    }
}
