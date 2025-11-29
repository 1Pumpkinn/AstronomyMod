package hs.astronomymod.abilities.neutronstar.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class GravitationalShockwaveActive implements ActiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 0;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(10),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = entityPos.subtract(playerPos).normalize();

            entity.setVelocity(direction.multiply(1.5).add(0, 0.5, 0));
            entity.velocityModified = true;

            float damage = 6.0f;
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), damage);
        });

        spawnShockwaveEffects(player);
    }

    private void spawnShockwaveEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS,
                2.0f, 0.6f);

        // Expanding rings
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

