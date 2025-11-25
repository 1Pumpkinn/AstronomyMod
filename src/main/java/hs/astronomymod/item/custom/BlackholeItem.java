package hs.astronomymod.item.custom;

import hs.astronomymod.item.AstronomyItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class BlackholeItem extends AstronomyItem {
    public BlackholeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Event Horizon - absorption and void protection
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 2, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false, false));

        // Increased max health
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 40, 3, false, false, true));

        // Void walker - slow falling and water breathing
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false, false));

        // Slight gravity pull on nearby items
        if (player.age % 10 == 0) {
            List<net.minecraft.entity.ItemEntity> items = player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    player.getBoundingBox().expand(8),
                    e -> true
            );
            items.forEach(item -> {
                Vec3d direction = player.getEntityPos().subtract(item.getEntityPos()).normalize();
                item.setVelocity(direction.multiply(0.3));
            });
        }

        // Dark void particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 8 == 0) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 5; i++) {
                double angle = player.age * 0.15 + i * (Math.PI * 2 / 5);
                double radius = 1.5;
                serverWorld.spawnParticles(ParticleTypes.PORTAL,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 0.5 + Math.sin(angle * 2) * 0.5,
                        pos.z + Math.sin(angle) * radius,
                        2, 0.1, 0.1, 0.1, 0.05);
                serverWorld.spawnParticles(ParticleTypes.SQUID_INK,
                        pos.x + Math.cos(angle) * radius * 0.7,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * radius * 0.7,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Singularity Collapse - extreme gravity well
        Vec3d playerPos = player.getEntityPos();
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(20),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            double distance = entityPos.distanceTo(playerPos);

            // Stronger pull the closer they are
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            double pullStrength = Math.max(2.0 - (distance * 0.1), 0.3);
            entity.setVelocity(direction.multiply(pullStrength));
            entity.velocityModified = true;

            // Damage increases as they get closer
            float damage = (float) Math.max(8.0 - distance * 0.3, 2.0);
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), damage);

            // Crushing effects
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 5));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 2));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 120, 3));

            // Void sickness
            if (distance < 5) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));
            }
        });

        // Player becomes ethereal
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 140, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 140, 3, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 140, 2, false, false, true));

        // Gravity collapse particles and sound
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS,
                    1.5f, 0.5f);

            Vec3d pos = player.getEntityPos();
            // Create swirling vortex
            for (int layer = 0; layer < 5; layer++) {
                double layerRadius = 15 - layer * 3;
                for (int i = 0; i < 100; i++) {
                    double angle = (i / 100.0) * Math.PI * 2 + layer * 0.5;
                    double height = layer * 0.8;
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                            pos.x + Math.cos(angle) * layerRadius,
                            pos.y + height,
                            pos.z + Math.sin(angle) * layerRadius,
                            1, 0, 0, 0, 0.3);
                }
            }

            // Dark center
            for (int i = 0; i < 50; i++) {
                serverWorld.spawnParticles(ParticleTypes.SQUID_INK,
                        pos.x, pos.y + 1, pos.z,
                        2,
                        Math.random() - 0.5,
                        Math.random() - 0.5,
                        Math.random() - 0.5, 0.1);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§5Passive: Event Horizon"));
        tooltip.accept(Text.literal("  §7• Void Protection"));
        tooltip.accept(Text.literal("  §7• Massive Health Boost"));
        tooltip.accept(Text.literal("  §7• Item Magnetism"));
        tooltip.accept(Text.literal("§8Active: Singularity Collapse"));
        tooltip.accept(Text.literal("  §7• Extreme Gravity Well"));
        tooltip.accept(Text.literal("  §7• Crushing Damage"));
        tooltip.accept(Text.literal("§e+ Ethereal Form"));
        tooltip.accept(Text.literal("§e+ Enhanced Speed"));
    }
}