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
    protected void addCustomTooltip(Consumer<Text> tooltip, net.minecraft.item.ItemStack stack) {
        int shards = stack.getOrDefault(
                hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0
        );

        tooltip.accept(Text.literal("§bPassive 1: No Fall Damage"));
        if (shards >= 1) {
            tooltip.accept(Text.literal("  §a✓ Active"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 1 shard"));
        }

        tooltip.accept(Text.literal("§bPassive 2: Haste I"));
        if (shards >= 2) {
            tooltip.accept(Text.literal("  §a✓ Active"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 2 shards"));
        }

        tooltip.accept(Text.literal("§eAbility 1: Lightning Strike"));
        tooltip.accept(Text.literal("  §7• 5x5 radius"));
        tooltip.accept(Text.literal("  §7• 3 hearts damage"));

        tooltip.accept(Text.literal("§eAbility 2: Gravity Fling"));
        tooltip.accept(Text.literal("  §7• Fling up then force down"));
    }
}