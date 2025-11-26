package hs.astronomymod.item.custom;

import hs.astronomymod.abilities.pulsar.PulsarAbility;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PulsarItem extends AstronomyItem {
    public PulsarItem(Settings settings) {
        super(settings, new PulsarAbility());
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§3Passive: Rhythmic Pulses"));
        tooltip.accept(Text.literal("  §7• Periodic Speed & Haste"));
        tooltip.accept(Text.literal("  §7• Enhanced Jump"));
        tooltip.accept(Text.literal("  §7• Electromagnetic Sight"));
        tooltip.accept(Text.literal("  §7• Energy Regeneration"));
        tooltip.accept(Text.literal("§bActive: EM Superpulse"));
        tooltip.accept(Text.literal("  §7• Area Sensory Overload"));
        tooltip.accept(Text.literal("  §7• Complete Disorientation"));
        tooltip.accept(Text.literal("§e+ Hypercharge Mode"));
        tooltip.accept(Text.literal("§e+ Extreme Mobility"));
    }
}

// REPLACE YOUR EXISTING PulsarItem.java FILE WITH THIS VERSION
// Location: src/main/java/hs/astronomymod/item/custom/PulsarItem.java