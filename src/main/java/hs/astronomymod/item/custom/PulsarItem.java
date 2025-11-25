package hs.astronomymod.item.custom;

import hs.astronomymod.AstronomyItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class PulsarItem extends AstronomyItem {
    public PulsarItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Energy pulses - periodic speed boost
        if (player.age % 60 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, false, false, false));
        }

        // Upside 1: Jump boost
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, 1, false, false, true));

        // Pulse particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 20 == 0) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2 * i) / 10;
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        pos.x + Math.cos(angle) * 2,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * 2,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Electromagnetic Pulse - blind and confuse enemies
        List<net.minecraft.entity.LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(12),
                e -> e != player
        );
        nearbyEntities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
        });

        // Upside 2: Glowing to see through walls
        List<net.minecraft.entity.LivingEntity> distantEntities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(30),
                e -> e != player
        );
        distantEntities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
        });

        // Burst effect
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 40; i++) {
                double angle = Math.random() * Math.PI * 2;
                double pitch = Math.random() * Math.PI - Math.PI / 2;
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y + 1, pos.z,
                        1,
                        Math.cos(angle) * Math.cos(pitch) * 3,
                        Math.sin(pitch) * 3,
                        Math.sin(angle) * Math.cos(pitch) * 3,
                        0.3);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§3Passive: Energy Pulses"));
        tooltip.accept(Text.literal("§bActive: EMP Burst"));
        tooltip.accept(Text.literal("§e+ Jump Boost"));
        tooltip.accept(Text.literal("§e+ Enemy Detection"));
    }
}