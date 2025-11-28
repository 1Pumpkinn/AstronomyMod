package hs.astronomymod.abilities.blackhole.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class GravitationalPullActive implements ActiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 0;
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
            double pullStrength = 1.2;

            entity.setVelocity(direction.multiply(pullStrength));
            entity.velocityModified = true;
        });

        spawnAbilityEffects(player);
    }

    private void spawnAbilityEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.5f);

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
}

