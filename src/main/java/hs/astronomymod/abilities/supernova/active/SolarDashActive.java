package hs.astronomymod.abilities.supernova.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SolarDashActive implements ActiveAbilityComponent {

    @Override
    public int getRequiredShards() {
        return 0;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        Vec3d lookDirection = player.getRotationVector();
        Vec3d dashVelocity = lookDirection.multiply(2.5);
        Vec3d startPos = player.getEntityPos();

        player.setVelocity(dashVelocity);
        player.velocityModified = true;

        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(5),
                e -> e != player
        );

        Vec3d endPos = startPos.add(lookDirection.multiply(5));

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            double distanceToLine = distanceToLine(entityPos, startPos, endPos);
            if (distanceToLine < 2.0) {
                entity.setOnFireFor(8);
                entity.damage(player.getEntityWorld().toServerWorld(),
                        player.getDamageSources().magic(), 4.0f);
            }
        });

        spawnAbilityEffects(player, lookDirection);
    }

    private void spawnAbilityEffects(ServerPlayerEntity player, Vec3d lookDir) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);

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

