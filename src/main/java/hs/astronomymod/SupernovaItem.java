package hs.astronomymod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class SupernovaItem extends AstronomyItem {
    public SupernovaItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Fire Resistance
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, false));

        // Upside 1: Night Vision
        if (player.age % 220 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, false, false, true));
        }

        // Particle effects
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 5 == 0) {
            Vec3d pos = player.getEntityPos();
            serverWorld.spawnParticles(ParticleTypes.FLAME, pos.x, pos.y + 1, pos.z, 2, 0.3, 0.5, 0.3, 0.01);
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Explosive Burst - damages nearby entities
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(8),
                e -> e != player
        );
        entities.forEach(entity -> {
            entity.damage(player.getEntityWorld().toServerWorld(), player.getDamageSources().explosion(player, player), 8.0f);
            entity.setFireTicks(100);
        });

        // Upside 2: Speed boost after explosion
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2, false, false, true));

        // Visual effect
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 50; i++) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                        pos.x, pos.y + 1, pos.z, 1,
                        Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2, 0.5);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§cPassive: Fire Immunity"));
        tooltip.accept(Text.literal("§6Active: Explosive Burst"));
        tooltip.accept(Text.literal("§e+ Night Vision"));
        tooltip.accept(Text.literal("§e+ Speed after explosion"));
    }
}