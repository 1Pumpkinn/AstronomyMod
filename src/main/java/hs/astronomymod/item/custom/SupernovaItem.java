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
                hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS,
                0
        );

        if (shards >= 1) {
            tooltip.accept(Text.literal("§c1 Shard: Stellar Heat"));
            tooltip.accept(Text.literal("  §7• Fire Immunity"));
            tooltip.accept(Text.literal("  §7• Night Vision"));
        } else {
            tooltip.accept(Text.literal("§81 Shard: ???"));
        }

        if (shards >= 2) {
            tooltip.accept(Text.literal("§c2 Shards: Solar Flare"));
            tooltip.accept(Text.literal("  §7• Burning Aura"));
            tooltip.accept(Text.literal("  §7• Speed & Strength"));
        } else {
            tooltip.accept(Text.literal("§82 Shards: ???"));
        }

        if (shards >= 3) {
            tooltip.accept(Text.literal("§63 Shards: Supernova Explosion"));
            tooltip.accept(Text.literal("§c1 Shard: Stellar Heat"));
            tooltip.accept(Text.literal("  §7• Fire Immunity"));
            tooltip.accept(Text.literal("  §7• Night Vision"));
        } else {
            tooltip.accept(Text.literal("§81 Shard: ???"));
        }
    }
}