package hs.astronomymod.abilities.blackhole;

import hs.astronomymod.abilities.Ability;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlackholeAbility implements Ability {

    @Override
    public void applyPassive(ServerPlayerEntity player) {
        net.minecraft.item.ItemStack astronomyStack = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack();
        int shards = astronomyStack.getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);

        // Apply passive effects based on shard count
        if (shards >= 1) {
            applyPassiveEffect1(player);
        }

        if (shards >= 2) {
            applyPassiveEffect2(player);
        }

        // Visual particles (always active)
        spawnPassiveParticles(player);
    }

    @Override
    public void applyActive(ServerPlayerEntity player) {
        int shards = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack()
                .getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);

        if (shards < 3) {
            // Ability 1: Gravitational Pull
            applyActiveAbility1(player);
        } else {
            // Ability 2: Singularity Collapse (requires 3 shards)
            applyActiveAbility2(player);
        }
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // Client-side visual effects if needed
    }

    // ========================================
    //         PASSIVE EFFECTS
    // ========================================

    /**
     * Passive Effect 1 (1+ shards): Nearby entities get slowness in 6x6 radius
     */
    private void applyPassiveEffect1(ServerPlayerEntity player) {
        List<LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        nearbyEntities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 40, 2, false, false, true
            ));
        });
    }

    /**
     * Passive Effect 2 (2+ shards): Density/Mace damage immunity
     */
    private void applyPassiveEffect2(ServerPlayerEntity player) {
        // Grant resistance when falling to negate mace density damage
        if (player.fallDistance > 0) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 2, 4, false, false, false
            ));
        }
    }

    /**
     * Visual particles for passive effect
     */
    private void spawnPassiveParticles(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        if (player.age % 8 != 0) return;

        Vec3d pos = player.getEntityPos();

        // Dark portal swirl
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

    // ========================================
    //         ACTIVE ABILITIES
    // ========================================

    /**
     * Active Ability 1: Gravitational Pull
     * Pulls entities towards player in 6 block radius
     */
    private void applyActiveAbility1(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();

        // Find and pull entities
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            double pullStrength = 1.2;

            entity.setVelocity(direction.multiply(pullStrength));
            entity.velocityModified = true;
        });

        // Sound and particle effects
        spawnAbility1Effects(player);
    }

    /**
     * Active Ability 2: Singularity Collapse (3 shards required)
     * Extreme pull + hides hearts with custom status effect
     */
    private void applyActiveAbility2(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();

        // Find and affect entities
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            double pullStrength = 2.5;

            // Extreme gravitational pull
            entity.setVelocity(direction.multiply(pullStrength));
            entity.velocityModified = true;

            // Hide hearts with blindness + darkness
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 200, 0, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DARKNESS, 200, 0, false, false, true
            ));

            // Additional crushing effects
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 200, 5, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 200, 3, false, false, true
            ));

            // Damage
            float damage = 8.0f;
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), damage);
        });

        // Player becomes ethereal
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.INVISIBILITY, 140, 0, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 140, 3, false, false, true
        ));

        // Sound and particle effects
        spawnAbility2Effects(player);
    }

    // ========================================
    //      PARTICLE & SOUND EFFECTS
    // ========================================

    /**
     * Visual/audio effects for Ability 1
     */
    private void spawnAbility1Effects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        // Sound
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);

        // Pulling vortex rings
        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 6 - ring * 2;
            for (int i = 0; i < 40; i++) {
                double angle = (i / 40.0) * Math.PI * 2;
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x + Math.cos(angle) * ringRadius,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * ringRadius,
                        1, 0, 0, 0, 0.2);
            }
        }
    }

    /**
     * Visual/audio effects for Ability 2
     */
    private void spawnAbility2Effects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        // Dramatic sound
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.5f, 0.5f);

        // Massive swirling vortex
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