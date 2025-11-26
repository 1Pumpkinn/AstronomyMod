package hs.astronomymod.abilities.supernova;

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
import net.minecraft.world.World;

import java.util.List;

public class SupernovaAbility implements Ability {

    @Override
    public void applyPassive(ServerPlayerEntity player) {

        // Passive buffs
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false, true));

        // Burning aura
        if (player.age % 20 == 0) {
            List<LivingEntity> nearby = player.getEntityWorld().getEntitiesByClass(
                    LivingEntity.class,
                    player.getBoundingBox().expand(3),
                    e -> e != player
            );

            nearby.forEach(e -> e.setOnFireFor(3));
        }

        // Flame particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 5 == 0) {
            Vec3d pos = player.getEntityPos();

            for (int i = 0; i < 2; i++) {
                serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        pos.x + (Math.random() - 0.5) * 0.8,
                        pos.y + 0.5 + Math.random() * 1.5,
                        pos.z + (Math.random() - 0.5) * 0.8,
                        1, 0, 0, 0, 0.02
                );
            }
        }
    }

    @Override
    public void applyActive(ServerPlayerEntity player) {

        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
        Vec3d playerPos = player.getEntityPos();

        // Explosion (1.21+ API)
        serverWorld.createExplosion(
                player,
                player.getDamageSources().explosion(player, player),
                null,
                playerPos.x,
                playerPos.y,
                playerPos.z,
                4.0f,
                false,
                World.ExplosionSourceType.MOB
        );

        // Damage + knockback
        List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(12),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d ePos = entity.getEntityPos();
            double dist = ePos.distanceTo(playerPos);

            Vec3d dir = ePos.subtract(playerPos).normalize();
            double kb = Math.max(3.0 - (dist * 0.15), 0.5);

            entity.setVelocity(dir.multiply(kb).add(0, 0.8, 0));
            entity.velocityModified = true;

            float damage = (float) Math.max(15.0 - dist * 0.8, 4.0);
            entity.damage(serverWorld, player.getDamageSources().magic(), damage);

            entity.setOnFireFor(8);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1));
        });

        // Player temporary buffs
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 0, false, false, true));

        // Explosion sound + particles
        serverWorld.playSound(
                null,
                playerPos.x,
                playerPos.y,
                playerPos.z,
                SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE,
                SoundCategory.PLAYERS,
                2.0f,
                0.8f
        );

        Vec3d pos = player.getEntityPos();

        // Expanding fire rings
        for (int ring = 0; ring < 5; ring++) {

            double radius = 2 + ring * 2.5;

            for (int i = 0; i < 40; i++) {
                double angle = (i / 40.0) * Math.PI * 2;

                double x = pos.x + Math.cos(angle) * radius;
                double z = pos.z + Math.sin(angle) * radius;

                serverWorld.spawnParticles(
                        ParticleTypes.FLAME,
                        x,
                        pos.y + 0.5,
                        z,
                        3, 0.2, 0.5, 0.2, 0.1
                );

                serverWorld.spawnParticles(
                        ParticleTypes.LAVA,
                        x,
                        pos.y + 0.5,
                        z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Central burst
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double vert = Math.random() * Math.PI;
            double speed = 0.5 + Math.random() * 0.5;

            serverWorld.spawnParticles(
                    ParticleTypes.FLAME,
                    pos.x,
                    pos.y + 1,
                    pos.z,
                    1,
                    Math.cos(angle) * Math.sin(vert) * speed,
                    Math.cos(vert) * speed,
                    Math.sin(angle) * Math.sin(vert) * speed,
                    0
            );
        }
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // client visuals if needed
    }
}
