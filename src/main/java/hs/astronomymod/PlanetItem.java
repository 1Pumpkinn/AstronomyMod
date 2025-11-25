package hs.astronomymod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class PlanetItem extends AstronomyItem {
    public PlanetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Increased gravity resistance (slow falling effect)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, false, false, false));

        // Upside 1: Regeneration
        if (player.age % 60 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0, false, false, true));
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Gravitational Pull - pulls nearby entities (simulated with slow)
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(10),
                e -> e != player
        );
        entities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
            // Upside 2: Damage resistance when using active ability
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, false, false, true));
        });
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§bPassive: Gravity Control"));
        tooltip.accept(Text.literal("§aActive: Gravitational Pull"));
        tooltip.accept(Text.literal("§e+ Regeneration"));
        tooltip.accept(Text.literal("§e+ Resistance when active"));
    }
}