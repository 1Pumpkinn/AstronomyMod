package hs.astronomymod.abilities.planet.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GravityFlingActive implements ActiveAbilityComponent {
    private static final Map<UUID, FlingState> flingStates = new ConcurrentHashMap<>();
    private static final int FLING_UP_TICKS = 20;
    private static final int FLING_DOWN_TICKS = 40;

    @Override
    public int getRequiredShards() {
        return 3;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            UUID uuid = entity.getUuid();
            flingStates.put(uuid, new FlingState(FLING_UP_TICKS, FLING_DOWN_TICKS, entity.getEntityPos()));
            
            // Initial upward fling
            entity.setVelocity(0, 1.5, 0);
            entity.velocityModified = true;
        });

        spawnAbilityEffects(player);
    }

    public static void tickFlingState(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(10),
                e -> e != player
        );

        entities.forEach(entity -> {
            UUID uuid = entity.getUuid();
            FlingState state = flingStates.get(uuid);
            
            if (state == null) return;

            if (state.upTicks > 0) {
                state.upTicks--;
                // Keep pushing up
                entity.setVelocity(entity.getVelocity().multiply(0.9, 1.0, 0.9).add(0, 0.1, 0));
                entity.velocityModified = true;
            } else if (state.downTicks > 0) {
                state.downTicks--;
                // Force down
                Vec3d currentPos = entity.getEntityPos();
                Vec3d direction = state.startPos.subtract(currentPos).normalize();
                entity.setVelocity(direction.multiply(0.5).add(0, -1.5, 0));
                entity.velocityModified = true;
            } else {
                // Done, remove state
                flingStates.remove(uuid);
            }
        });
    }

    private void spawnAbilityEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d pos = player.getEntityPos();

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.8f);

        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 2;
            double radius = 3;
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    pos.x + Math.cos(angle) * radius,
                    pos.y + 1,
                    pos.z + Math.sin(angle) * radius,
                    2, 0.2, 0.3, 0.2, 0.05);
        }
    }

    private static class FlingState {
        int upTicks;
        int downTicks;
        Vec3d startPos;

        FlingState(int upTicks, int downTicks, Vec3d startPos) {
            this.upTicks = upTicks;
            this.downTicks = downTicks;
            this.startPos = startPos;
        }
    }
}

