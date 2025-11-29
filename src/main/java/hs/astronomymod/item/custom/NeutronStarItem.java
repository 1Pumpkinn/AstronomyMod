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
    protected void addCustomTooltip(Consumer<Text> tooltip, net.minecraft.item.ItemStack stack) {
        int shards = stack.getOrDefault(
                hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0
        );

        tooltip.accept(Text.literal("§7Passive 1: Immovable"));
        if (shards >= 1) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• Knockback immunity"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 1 shard"));
        }

        tooltip.accept(Text.literal("§7Passive 2: Deflection Field"));
        if (shards >= 2) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• Projectiles bend away"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 2 shards"));
        }

        tooltip.accept(Text.literal("§fAbility 1: Gravity Shockwave"));
        tooltip.accept(Text.literal("  §7• Blast enemies outward"));
        tooltip.accept(Text.literal("  §7• 10 block radius"));

        tooltip.accept(Text.literal("§fAbility 2: Plasma Beam"));
        tooltip.accept(Text.literal("  §7• Toggle 10s beam"));
        tooltip.accept(Text.literal("  §7• 0.5 hearts/tick"));
    }
}