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
    protected void addCustomTooltip(Consumer<Text> tooltip, net.minecraft.item.ItemStack stack) {
        int shards = stack.getOrDefault(
                hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0
        );

        tooltip.accept(Text.literal("§cPassive 1: Fire Aspect"));
        if (shards >= 1) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• All weapons ignite enemies"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 1 shard"));
        }

        tooltip.accept(Text.literal("§cPassive 2: Solar Eruption"));
        if (shards >= 2) {
            tooltip.accept(Text.literal("  §a✓ Active"));
            tooltip.accept(Text.literal("  §7• Triggers at 3 hearts"));
            tooltip.accept(Text.literal("  §7• 5s invulnerability"));
        } else {
            tooltip.accept(Text.literal("  §8✗ Requires 2 shards"));
        }

        tooltip.accept(Text.literal("§6Ability 1: Solar Dash"));
        tooltip.accept(Text.literal("  §7• Dash 5 blocks"));
        tooltip.accept(Text.literal("  §7• Ignite enemies in path"));

        tooltip.accept(Text.literal("§6Ability 2: Solar Flare"));
        tooltip.accept(Text.literal("  §7• Massive explosion"));
        tooltip.accept(Text.literal("  §7• No terrain damage"));
    }
}