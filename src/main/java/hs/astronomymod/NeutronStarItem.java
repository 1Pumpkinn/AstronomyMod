package hs.astronomymod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class NeutronStarItem extends AstronomyItem {
    public NeutronStarItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Extreme density - knockback resistance
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false, false));

        // Upside 1: Increased attack damage
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, false, false, true));
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Magnetic Field - stun nearby enemies
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(7),
                e -> e != player
        );
        entities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 10));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        });

        // Upside 2: Haste boost
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200, 2, false, false, true));
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§7Passive: Dense Armor"));
        tooltip.accept(Text.literal("§fActive: Magnetic Field"));
        tooltip.accept(Text.literal("§e+ Strength"));
        tooltip.accept(Text.literal("§e+ Haste when active"));
    }
}