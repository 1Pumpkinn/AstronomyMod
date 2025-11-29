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
import net.minecraft.world.World;

/**
 * Gravity Fling active ability - launches nearby entities up, then slams them down until they land, where an explosion/particle effect plays.
 * Enhanced with a true slam plus impact visuals.
 */
public class GravityFlingActive implements ActiveAbilityComponent {
    private static final Map<UUID, FlingState> flingStates = new ConcurrentHashMap<>();
    private static final int FLING_UP_TICKS = 20; // Ticks to keep moving up
    private static final int FLING_SLAM_WAIT = 5; // Small delay before slam
    private static final double FLING_RADIUS = 6.0;
    private static final double FLING_SLAM_RADIUS = 10.0;
    private static final double MIN_Y_MOTION = 0.6;
    private static final double SLAM_FORCE = -2.2;
    @Override
    public int getRequiredShards() { return 3; }
    @Override
    public void activate(ServerPlayerEntity player) {
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(FLING_RADIUS),
                e -> e != player && Math.abs(e.getVelocity().y) < MIN_Y_MOTION
        );
        if (entities.isEmpty()) {
            player.sendMessage(net.minecraft.text.Text.literal("§7No valid targets for Gravity Fling!"), true);
            return;
        }
        entities.forEach(entity -> {
            UUID uuid = entity.getUuid();
            flingStates.put(uuid, new FlingState(FLING_UP_TICKS, false, entity.getEntityPos()));
            entity.setVelocity(0, 1.5, 0);
            entity.velocityModified = true;
        });
        spawnAbilityEffects(player);
    }
    /**
     * Must be called every tick for the activating player to process entity states.
     */
    public static void tickFlingState(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(FLING_SLAM_RADIUS),
                e -> e != player
        );
        for (LivingEntity entity : entities) {
            UUID uuid = entity.getUuid();
            FlingState state = flingStates.get(uuid);
            if (state == null) continue;
            if (!state.slamStarted) {
                if (state.upTicks > 0) {
                    state.upTicks--;
                    if (entity.getVelocity().y < 1.3) {
                        entity.setVelocity(entity.getVelocity().multiply(0.9, 1.0, 0.9).add(0, 0.1, 0));
                        entity.velocityModified = true;
                    }
                } else {
                    state.slamStarted = true;
                    state.slamDelay = FLING_SLAM_WAIT;
                }
            } else if (state.slamDelay > 0) {
                state.slamDelay--;
            } else if (!state.impactPlayed) {
                // Apply strong downward force during slam
                entity.setVelocity(entity.getVelocity().x, SLAM_FORCE, entity.getVelocity().z);
                entity.velocityModified = true;
                // Check for ground touch: if onGround or feet Y unchanged, then impact (avoid bounce repeats)
                boolean onGround = entity.isOnGround();
                boolean landed = (Math.abs(entity.getVelocity().y) < 0.2 && entity.isOnGround());
                if (onGround && landed) {
                    playImpactEffect(serverWorld, entity.getEntityPos());
                    state.impactPlayed = true;
                    // Remove immediately after impact
                    flingStates.remove(uuid);
                }
            }
        }
    }
    /** Triggers a non-damaging explosion and particle effect at the given position. */
    private static void playImpactEffect(ServerWorld world, Vec3d pos) {
        // Visual explosion (no damage, no terrain)
        world.createExplosion(null, pos.x, pos.y, pos.z, 1.2F, false, World.ExplosionSourceType.NONE);
        // Play cloud ring as extra flair
        for (int i = 0; i < 24; i++) {
            double angle = (i / 24.0) * Math.PI * 2;
            double outRad = 1.2 + Math.random() * 0.4;
            world.spawnParticles(ParticleTypes.CLOUD,
                    pos.x + Math.cos(angle) * outRad,
                    pos.y + 0.2,
                    pos.z + Math.sin(angle) * outRad,
                    2, 0.12, 0.10, 0.12, 0.08);
        }
        // Play a poof/explosion sound
        world.playSound(null, pos.x, pos.y, pos.z,
                net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EXPLODE,
                net.minecraft.sound.SoundCategory.PLAYERS, 0.75f, 1.07f + (float) (Math.random() * 0.15 - 0.08));
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
        boolean slamStarted;
        int slamDelay;
        Vec3d startPos;
        boolean impactPlayed = false;
        FlingState(int upTicks, boolean slamStarted, Vec3d startPos) {
            this.upTicks = upTicks;
            this.slamStarted = slamStarted;
            this.startPos = startPos;
        }
    }
}

