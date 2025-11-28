package hs.astronomymod.abilities.planet;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PlanetAbility implements Ability {
    @Override
    public void applyPassive(ServerPlayerEntity player) {
        // Fetch the equipped astronomy item stack
        net.minecraft.item.ItemStack astronomyStack = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack();
        int shards = astronomyStack.getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);
        
        // Passive Effect 1: Slow Falling & Step Assist (requires exactly 1 shard or more)
        if (shards >= 1) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOW_FALLING, 40, 0, false, false, false
            ));
            // Step assist (like a horse)
            player.getAttributeInstance(EntityAttributes.STEP_HEIGHT)
                    .setBaseValue(1.0);
        }
        
        // Passive Effect 2: Periodic Regeneration (requires exactly 2 shards or more)
        if (shards >= 2) {
            if (player.age % 80 == 0) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION, 80, 1, false, false, true
                ));
            }
        }

        // Atmospheric particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 10 == 0) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 3; i++) {
                double angle = (player.age + i * 120) * 0.05;
                double radius = 1.5;
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean applyActive(ServerPlayerEntity player, AbilityActivation activation) {
        // Active: Gravitational Singularity - massive pull and levitation
        Vec3d playerPos = player.getEntityPos();
        List<net.minecraft.entity.LivingEntity> entities =
                player.getEntityWorld().getEntitiesByClass(
                        net.minecraft.entity.LivingEntity.class,
                        player.getBoundingBox().expand(15),
                        e -> e != player
                );

        entities.forEach(entity -> {
            // Strong pull toward player
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            entity.setVelocity(direction.multiply(1.2));

            // Apply levitation and slowness
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 2));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        });

        // Player gets massive resistance and absorption
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 120, 2, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.ABSORPTION, 120, 3, false, false, true
        ));

        // Gravitational vortex particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 60; i++) {
                double angle = (i / 60.0) * Math.PI * 2;
                double radius = 3 + (i % 10);
                serverWorld.spawnParticles(ParticleTypes.PORTAL,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 0.5 + (i % 5) * 0.5,
                        pos.z + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0.1);
            }
        }

        return true;
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {

    }
}
