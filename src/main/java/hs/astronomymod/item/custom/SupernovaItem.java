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

public class SupernovaItem extends AstronomyItem {
    public SupernovaItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Stellar Heat - fire resistance and burning aura
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, false));

        // Damage nearby entities with heat aura
        if (player.age % 20 == 0) {
            List<net.minecraft.entity.LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class,
                    player.getBoundingBox().expand(3),
                    e -> e != player
            );
            nearbyEntities.forEach(entity -> {
                entity.setFireTicks(40);
                entity.damage(player.getEntityWorld().toServerWorld(), player.getDamageSources().onFire(), 1.0f);
            });
        }

        // Permanent night vision for star gazing
        if (player.age % 220 == 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, false, false, true));
        }

        // Glowing effect to radiate light
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, false, false, false));

        // Stellar flame particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 3 == 0) {
            Vec3d pos = player.getEntityPos();
            double angle = player.age * 0.1;
            for (int i = 0; i < 2; i++) {
                serverWorld.spawnParticles(ParticleTypes.FLAME,
                        pos.x + Math.cos(angle + i * Math.PI) * 0.8,
                        pos.y + 0.5 + Math.random() * 1.5,
                        pos.z + Math.sin(angle + i * Math.PI) * 0.8,
                        1, 0, 0.1, 0, 0.02);
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Supernova Explosion - massive damage and knockback
        Vec3d playerPos = player.getEntityPos();
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(12),
                e -> e != player
        );

        entities.forEach(entity -> {
            // Calculate distance for damage falloff
            double distance = entity.getEntityPos().distanceTo(playerPos);
            float damage = (float) (15.0 - (distance * 0.5));

            // Explosive damage and fire
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().explosion(player, player),
                    Math.max(damage, 5.0f));
            entity.setFireTicks(200);

            // Knockback
            Vec3d direction = entity.getEntityPos().subtract(playerPos).normalize();
            entity.setVelocity(direction.multiply(2.0).add(0, 1.0, 0));
            entity.velocityModified = true;

            // Blindness from the flash
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
        });

        // Player buffs
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 3, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, false, false, true));

        // Explosion sound and particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS,
                    2.0f, 0.8f);

            Vec3d pos = player.getEntityPos();
            // Multiple explosion waves
            for (int wave = 0; wave < 3; wave++) {
                double waveRadius = 2 + wave * 3;
                for (int i = 0; i < 80; i++) {
                    double angle = (i / 80.0) * Math.PI * 2;
                    double vertAngle = (i % 10) * (Math.PI / 10);
                    serverWorld.spawnParticles(ParticleTypes.FLAME,
                            pos.x + Math.cos(angle) * waveRadius,
                            pos.y + 1 + Math.sin(vertAngle) * 2,
                            pos.z + Math.sin(angle) * waveRadius,
                            3, 0.2, 0.2, 0.2, 0.1);
                }
            }

            // Core explosion particles
            for (int i = 0; i < 100; i++) {
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                        pos.x, pos.y + 1, pos.z, 1,
                        (Math.random() - 0.5) * 6,
                        (Math.random() - 0.5) * 6,
                        (Math.random() - 0.5) * 6, 0.5);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§cPassive: Stellar Heat"));
        tooltip.accept(Text.literal("  §7• Fire Immunity"));
        tooltip.accept(Text.literal("  §7• Burning Aura"));
        tooltip.accept(Text.literal("  §7• Night Vision"));
        tooltip.accept(Text.literal("§6Active: Supernova Explosion"));
        tooltip.accept(Text.literal("  §7• Massive Damage & Knockback"));
        tooltip.accept(Text.literal("§e+ Speed & Strength Boost"));
        tooltip.accept(Text.literal("§e+ Resistance"));
    }
}