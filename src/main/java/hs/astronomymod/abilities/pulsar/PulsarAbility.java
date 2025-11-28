package hs.astronomymod.abilities.pulsar;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
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

public class PulsarAbility implements Ability {
    @Override
    public void applyPassive(ServerPlayerEntity player) {
        // Fetch the equipped astronomy item stack
        net.minecraft.item.ItemStack astronomyStack = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack();
        int shards = astronomyStack.getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);
        
        // Calculate pulse cycle (0-100 based on age) - used for both effects and particles
        int pulsePhase = player.age % 100;
        
        // Passive Effect 1: Periodic Speed & Haste (requires exactly 1 shard or more)
        if (shards >= 1) {
            // Enhanced effects during pulse (every 5 seconds)
            if (pulsePhase < 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 1, false, false, true));
            } else {
                // Base effects between pulses
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 0, false, false, true));
            }
        }
        
        // Passive Effect 2: Electromagnetic Sight (requires exactly 2 shards or more)
        if (shards >= 2) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
        }

        // Electromagnetic pulse particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 3 == 0) {
            Vec3d pos = player.getEntityPos();

            // Pulsing ring effect
            if (pulsePhase % 20 == 0) {
                double radius = 2.0;
                for (int i = 0; i < 20; i++) {
                    double angle = (i / 20.0) * Math.PI * 2;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + Math.cos(angle) * radius,
                            pos.y + 1,
                            pos.z + Math.sin(angle) * radius,
                            3, 0.1, 0.1, 0.1, 0.05);
                }
            }

            // Constant small sparks
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + (Math.random() - 0.5) * 0.6,
                    pos.y + 0.5 + Math.random() * 1.5,
                    pos.z + (Math.random() - 0.5) * 0.6,
                    1, 0, 0, 0, 0.02);
        }
    }

    @Override
    public boolean applyActive(ServerPlayerEntity player, AbilityActivation activation) {
        // Active: EM Superpulse - extreme electromagnetic pulse causing sensory overload
        Vec3d playerPos = player.getEntityPos();
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(15),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            double distance = entityPos.distanceTo(playerPos);

            // Electromagnetic repulsion
            Vec3d direction = entityPos.subtract(playerPos).normalize();
            entity.setVelocity(direction.multiply(1.0).add(0, 0.4, 0));
            entity.velocityModified = true;

            // Electric damage
            float damage = (float) Math.max(8.0 - distance * 0.4, 3.0);
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), damage);

            // Complete sensory overload
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 180, 4));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 180, 2));

            // Electromagnetic interference
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 180, 3));
        });

        // Player enters hypercharge mode
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 3, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 300, 3, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 300, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 1, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 1, false, false, true));

        // Massive electromagnetic pulse effects
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
                    1.5f, 1.5f);

            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS,
                    1.0f, 2.0f);

            Vec3d pos = player.getEntityPos();

            // Multiple expanding electromagnetic rings
            for (int wave = 0; wave < 6; wave++) {
                double waveRadius = 2 + wave * 2.5;
                for (int i = 0; i < 50; i++) {
                    double angle = (i / 50.0) * Math.PI * 2;
                    double heightVariation = Math.sin(angle * 4) * 0.5;

                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + Math.cos(angle) * waveRadius,
                            pos.y + 1 + heightVariation,
                            pos.z + Math.sin(angle) * waveRadius,
                            2, 0.1, 0.1, 0.1, 0.1);

                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                            pos.x + Math.cos(angle) * waveRadius,
                            pos.y + 1 + heightVariation,
                            pos.z + Math.sin(angle) * waveRadius,
                            1, 0, 0, 0, 0.05);
                }
            }

            // Central energy vortex
            for (int i = 0; i < 80; i++) {
                double angle = Math.random() * Math.PI * 2;
                double vertAngle = Math.random() * Math.PI;
                double speed = 0.4 + Math.random() * 0.4;

                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y + 1, pos.z,
                        1,
                        Math.cos(angle) * Math.sin(vertAngle) * speed,
                        Math.cos(vertAngle) * speed,
                        Math.sin(angle) * Math.sin(vertAngle) * speed, 0);
            }

            // Spiral beams
            for (int beam = 0; beam < 4; beam++) {
                double beamAngle = (beam / 4.0) * Math.PI * 2;
                for (int dist = 0; dist < 15; dist++) {
                    double spiralAngle = beamAngle + dist * 0.3;
                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                            pos.x + Math.cos(spiralAngle) * dist,
                            pos.y + 1 + Math.sin(dist * 0.5) * 0.5,
                            pos.z + Math.sin(spiralAngle) * dist,
                            1, 0, 0, 0, 0);
                }
            }
        }

        return true;
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // Client-side effects if needed
    }
}