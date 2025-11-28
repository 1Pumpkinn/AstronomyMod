package hs.astronomymod.abilities.supernova.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
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

public class SolarFlareActive implements ActiveAbilityComponent {

    @Override
    public int getRequiredShards() {
        return 3;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d playerPos = player.getEntityPos();

        serverWorld.createExplosion(
                player,
                player.getDamageSources().explosion(player, player),
                null,
                playerPos.x,
                playerPos.y,
                playerPos.z,
                4.0f,
                false,
                World.ExplosionSourceType.NONE
        );

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
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 100, 1
            ));
        });

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

        spawnAbilityEffects(serverWorld, playerPos);
    }

    private void spawnAbilityEffects(ServerWorld serverWorld, Vec3d pos) {
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 2.0f, 0.8f);

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
}

