package hs.astronomymod.abilities.blackhole.active;

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

import java.util.List;

public class SingularityCollapseActive implements ActiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 3;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();

        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            double pullStrength = 2.5;

            entity.setVelocity(direction.multiply(pullStrength));
            entity.velocityModified = true;

            // Hide hearts with invisibility (custom status effect equivalent)
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY, 200, 0, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 200, 0, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DARKNESS, 200, 0, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 200, 5, false, false, true
            ));
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, 200, 3, false, false, true
            ));

            float damage = 8.0f;
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), damage);
        });

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.INVISIBILITY, 140, 0, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 140, 3, false, false, true
        ));

        spawnAbilityEffects(player);
    }

    private void spawnAbilityEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.5f, 0.5f);

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

