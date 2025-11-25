package hs.astronomymod.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class NeutronStarItem extends hs.astronomymod.item.AstronomyItem {
    public NeutronStarItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Ultra-Dense Core - extreme resistance and strength
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 2, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 2, false, false, true));

        // Immovable - knockback resistance
        if (player.getEntityWorld() instanceof ServerWorld) {
            player.setVelocity(player.getVelocity().multiply(0.8, 1.0, 0.8));
        }

        // Dense armor - damage reduction aura
        List<LivingEntity> nearbyEnemies = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(4),
                e -> {
                    if (e == player) return false; // ignore self
                    if (e instanceof MobEntity mob) {
                        return mob.getTarget() == player; // true if mob is actively targeting player
                    }
                    return false; // ignore non-mobs
                }
        );
        if (!nearbyEnemies.isEmpty()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 1, false, false, true));
        }

        // Haste for mining
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 1, false, false, true));

        // Metallic shimmer particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 15 == 0) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 3; i++) {
                serverWorld.spawnParticles(ParticleTypes.FIREWORK,
                        pos.x + (Math.random() - 0.5) * 0.5,
                        pos.y + 0.5 + Math.random() * 1.5,
                        pos.z + (Math.random() - 0.5) * 0.5,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Magnetic Storm - massive electromagnetic pulse
        Vec3d playerPos = player.getEntityPos();
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(10),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            double distance = entityPos.distanceTo(playerPos);

            // Magnetic repulsion
            Vec3d direction = entityPos.subtract(playerPos).normalize();
            entity.setVelocity(direction.multiply(1.5).add(0, 0.5, 0));
            entity.velocityModified = true;

            // Crushing damage
            float damage = (float) (10.0 - distance * 0.5);
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), Math.max(damage, 4.0f));

            // Complete immobilization
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 150, 10));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 150, 3));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 150, 5));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 150, 128)); // Negative jump = can't jump

            // Disorientation
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 150, 1));
        });

        // Player becomes an unstoppable force
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 240, 4, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 4, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 240, 1, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 240, 3, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 240, 10, false, false, true));

        // Magnetic field effects
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS,
                    2.0f, 0.6f);

            Vec3d pos = player.getEntityPos();

            // Magnetic field lines
            for (int ring = 0; ring < 4; ring++) {
                double ringRadius = 3 + ring * 2;
                for (int i = 0; i < 60; i++) {
                    double angle = (i / 60.0) * Math.PI * 2;
                    double heightOffset = Math.sin(angle * 4) * 1.5;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + Math.cos(angle) * ringRadius,
                            pos.y + 1 + heightOffset,
                            pos.z + Math.sin(angle) * ringRadius,
                            1, 0, 0, 0, 0.1);
                }
            }

            // Core pulse
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double vertAngle = Math.random() * Math.PI;
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        pos.x, pos.y + 1, pos.z,
                        1,
                        Math.cos(angle) * Math.sin(vertAngle),
                        Math.cos(vertAngle),
                        Math.sin(angle) * Math.sin(vertAngle), 0.3);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§7Passive: Ultra-Dense Core"));
        tooltip.accept(Text.literal("  §7• Extreme Resistance"));
        tooltip.accept(Text.literal("  §7• Massive Strength"));
        tooltip.accept(Text.literal("  §7• Knockback Immunity"));
        tooltip.accept(Text.literal("§fActive: Magnetic Storm"));
        tooltip.accept(Text.literal("  §7• Complete Immobilization"));
        tooltip.accept(Text.literal("  §7• Massive Repulsion"));
        tooltip.accept(Text.literal("§e+ Unstoppable Force"));
        tooltip.accept(Text.literal("§e+ Enhanced Mining"));
    }
}