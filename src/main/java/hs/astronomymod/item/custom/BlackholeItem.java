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
    protected void addCustomTooltip(Consumer<Text> tooltip, net.minecraft.item.ItemStack stack) {
        int shards = stack.getOrDefault(
                hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0
        );

        tooltip.accept(Text.literal("§5Passive 1: Gravity Well"));
        if (shards >= 1) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• Slowness to nearby entities"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 1 shard"));
        }

        tooltip.accept(Text.literal("§5Passive 2: Density Shield"));
        if (shards >= 2) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• Mace damage immunity"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 2 shards"));
        }

        tooltip.accept(Text.literal("§8Ability 1: Gravitational Pull"));
        tooltip.accept(Text.literal("  §7• 6x6 radius pull"));

        tooltip.accept(Text.literal("§8Ability 2: Singularity Collapse"));
        tooltip.accept(Text.literal("  §7• Hide enemy hearts"));
        tooltip.accept(Text.literal("  §7• Massive pull + damage"));
    }
}
