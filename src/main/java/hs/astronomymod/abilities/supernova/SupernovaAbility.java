package hs.astronomymod.abilities.supernova;

import hs.astronomymod.abilities.Ability;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SupernovaAbility implements Ability {

    private static final Identifier FIRE_ASPECT_ID = Identifier.of("astronomymod", "fire_aspect");
    private boolean hasTriggeredEruption = false;
    private int eruptionCooldown = 0;

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

        // Manage eruption cooldown
        if (eruptionCooldown > 0) {
            eruptionCooldown--;
        }
        if (player.getHealth() > 6.0f) {
            hasTriggeredEruption = false;
        }

        // Visual particles (scales with shards)
        spawnPassiveParticles(player, shards);
    }

    @Override
    public void applyActive(ServerPlayerEntity player) {
        int shards = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack()
                .getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);

        if (shards < 3) {
            // Ability 1: Solar Dash
            applyActiveAbility1(player);
        } else {
            // Ability 2: Solar Flare (requires 3 shards)
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
     * Passive Effect 1 (1+ shards): Fire Aspect on all weapons/fists
     */
    private void applyPassiveEffect1(ServerPlayerEntity player) {
        // Add fire aspect attribute modifier
        var attackDamage = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // Check if modifier already exists
            if (!attackDamage.hasModifier(FIRE_ASPECT_ID)) {
                attackDamage.addTemporaryModifier(
                        new EntityAttributeModifier(
                                FIRE_ASPECT_ID,
                                0.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
        }

        // Set entities on fire when player attacks (handled in damage calculation)
        // Alternative: Apply fire to nearby entities when attacking
        if (player.age % 20 == 0 && player.getAttacking() != null) {
            if (player.getAttacking() instanceof LivingEntity target) {
                target.setOnFireFor(4);
            }
        }
    }

    /**
     * Passive Effect 2 (2+ shards): Solar Eruption when below 3 hearts
     * Creates eruption, invulnerability for 5 seconds, blindness to enemies
     */
    private void applyPassiveEffect2(ServerPlayerEntity player) {
        // Check if player is below 3 hearts (6.0 health)
        if (player.getHealth() <= 6.0f && !hasTriggeredEruption && eruptionCooldown <= 0) {
            triggerSolarEruption(player);
            hasTriggeredEruption = true;
            eruptionCooldown = 600; // 30 second cooldown before can trigger again
        }
    }

    /**
     * Solar Eruption: Triggered when health drops below 3 hearts
     */
    private void triggerSolarEruption(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();

        // Player gets invulnerability for 5 seconds
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 4, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 100, 0, false, false, true
        ));

        // Affect entities in 6 block radius
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            // 5 seconds of blindness
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 100, 0, false, false, true
            ));

            // Set on fire
            entity.setOnFireFor(5);

            // Minor damage
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), 4.0f);
        });

        // Visual and sound effects
        spawnEruptionEffects(player);
    }

    /**
     * Visual particles for passive effect
     */
    private void spawnPassiveParticles(ServerPlayerEntity player, int shards) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        if (player.age % 5 != 0) return;

        Vec3d pos = player.getEntityPos();

        // Flame particles (intensity scales with shards)
        for (int i = 0; i < shards; i++) {
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x + (Math.random() - 0.5) * 0.8,
                    pos.y + 0.5 + Math.random() * 1.5,
                    pos.z + (Math.random() - 0.5) * 0.8,
                    1, 0, 0, 0, 0.02);
        }
    }

    // ========================================
    //         ACTIVE ABILITIES
    // ========================================

    /**
     * Active Ability 1: Solar Dash
     * Dashes player 5 blocks forward, ignites entities passed through
     */
    private void applyActiveAbility1(ServerPlayerEntity player) {
        Vec3d lookDirection = player.getRotationVector();
        Vec3d dashVelocity = lookDirection.multiply(2.5); // Dash 5 blocks forward

        // Store current position for trail effect
        Vec3d startPos = player.getEntityPos();

        // Apply dash velocity
        player.setVelocity(dashVelocity);
        player.velocityModified = true;

        // Find entities along the dash path and ignite them
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(5),
                e -> e != player
        );

        Vec3d endPos = startPos.add(lookDirection.multiply(5));

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();

            // Check if entity is roughly along the dash line
            double distanceToLine = distanceToLine(entityPos, startPos, endPos);
            if (distanceToLine < 2.0) {
                entity.setOnFireFor(8);

                // Minor damage
                entity.damage(player.getEntityWorld().toServerWorld(),
                        player.getDamageSources().magic(), 4.0f);
            }
        });

        // Visual and sound effects
        spawnAbility1Effects(player);
    }

    /**
     * Active Ability 2: Solar Flare (3 shards required)
     * Massive explosion around player, doesn't damage terrain
     */
    private void applyActiveAbility2(ServerPlayerEntity player) {
        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
        Vec3d playerPos = player.getEntityPos();

        // Visual explosion only (no terrain damage)
        serverWorld.createExplosion(
                player,
                player.getDamageSources().explosion(player, player),
                null,
                playerPos.x,
                playerPos.y,
                playerPos.z,
                4.0f,
                false,
                World.ExplosionSourceType.NONE // No terrain damage
        );

        // Damage and knockback entities
        List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(12),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d ePos = entity.getEntityPos();
            double dist = ePos.distanceTo(playerPos);

            // Knockback
            Vec3d dir = ePos.subtract(playerPos).normalize();
            double kb = Math.max(3.0 - (dist * 0.15), 0.5);

            entity.setVelocity(dir.multiply(kb).add(0, 0.8, 0));
            entity.velocityModified = true;

            // Damage (scales with distance)
            float damage = (float) Math.max(15.0 - dist * 0.8, 4.0);
            entity.damage(serverWorld, player.getDamageSources().magic(), damage);

            // Set on fire
            entity.setOnFireFor(8);

            // Weakness debuff
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 100, 1
            ));
        });

        // Player buffs
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 200, 2, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH, 200, 2, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 200, 2, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 200, 0, false, false, true
        ));

        // Visual and sound effects
        spawnAbility2Effects(player);
    }

    // ========================================
    //      PARTICLE & SOUND EFFECTS
    // ========================================

    /**
     * Visual/audio effects for Solar Dash (Ability 1)
     */
    private void spawnAbility1Effects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();
        Vec3d lookDir = player.getRotationVector();

        // Sound
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);

        // Fire trail
        for (int i = 0; i < 20; i++) {
            double progress = i / 20.0;
            Vec3d trailPos = pos.add(lookDir.multiply(5 * progress));

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    trailPos.x, trailPos.y + 1, trailPos.z,
                    3, 0.2, 0.2, 0.2, 0.05);

            serverWorld.spawnParticles(ParticleTypes.LAVA,
                    trailPos.x, trailPos.y + 1, trailPos.z,
                    1, 0.1, 0.1, 0.1, 0);
        }
    }

    /**
     * Visual/audio effects for Solar Eruption (Passive 2)
     */
    private void spawnEruptionEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        // Sound
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.5f, 1.0f);

        // Eruption rings
        for (int ring = 0; ring < 3; ring++) {
            double radius = 2 + ring * 2;
            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 2;
                serverWorld.spawnParticles(ParticleTypes.FLAME,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 0.5,
                        pos.z + Math.sin(angle) * radius,
                        2, 0.2, 0.3, 0.2, 0.1);
            }
        }

        // Central burst
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * Math.PI * 2;
            double vert = Math.random() * Math.PI;
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x, pos.y + 1, pos.z,
                    1,
                    Math.cos(angle) * Math.sin(vert) * 0.5,
                    Math.cos(vert) * 0.5,
                    Math.sin(angle) * Math.sin(vert) * 0.5,
                    0);
        }
    }

    /**
     * Visual/audio effects for Solar Flare (Ability 2)
     */
    private void spawnAbility2Effects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        // Sound
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 2.0f, 0.8f);

        // Expanding fire rings
        for (int ring = 0; ring < 5; ring++) {
            double radius = 2 + ring * 2.5;

            for (int i = 0; i < 40; i++) {
                double angle = (i / 40.0) * Math.PI * 2;

                double x = pos.x + Math.cos(angle) * radius;
                double z = pos.z + Math.sin(angle) * radius;

                serverWorld.spawnParticles(ParticleTypes.FLAME,
                        x, pos.y + 0.5, z,
                        3, 0.2, 0.5, 0.2, 0.1);

                serverWorld.spawnParticles(ParticleTypes.LAVA,
                        x, pos.y + 0.5, z,
                        1, 0, 0, 0, 0);
            }
        }

        // Central burst
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double vert = Math.random() * Math.PI;
            double speed = 0.5 + Math.random() * 0.5;

            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x, pos.y + 1, pos.z,
                    1,
                    Math.cos(angle) * Math.sin(vert) * speed,
                    Math.cos(vert) * speed,
                    Math.sin(angle) * Math.sin(vert) * speed,
                    0);
        }
    }

    // ========================================
    //         UTILITY METHODS
    // ========================================

    /**
     * Calculate distance from point to line segment
     */
    private double distanceToLine(Vec3d point, Vec3d lineStart, Vec3d lineEnd) {
        Vec3d line = lineEnd.subtract(lineStart);
        Vec3d pointToStart = point.subtract(lineStart);

        double lineLength = line.length();
        if (lineLength == 0) return pointToStart.length();

        double t = Math.max(0, Math.min(1, pointToStart.dotProduct(line) / (lineLength * lineLength)));
        Vec3d projection = lineStart.add(line.multiply(t));

        return point.distanceTo(projection);
    }
}