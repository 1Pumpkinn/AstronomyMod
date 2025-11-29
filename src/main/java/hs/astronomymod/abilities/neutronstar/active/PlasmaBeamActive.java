package hs.astronomymod.abilities.neutronstar.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlasmaBeamActive implements ActiveAbilityComponent {
    private static final Map<UUID, BeamState> beamStates = new ConcurrentHashMap<>();
    private static final int BEAM_DURATION_TICKS = 200; // 10 seconds

    @Override
    public int getRequiredShards() {
        return 3;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        BeamState state = beamStates.get(uuid);

        if (state != null && state.isActive) {
            // Already active, deactivate
            state.isActive = false;
            beamStates.remove(uuid);
            player.sendMessage(Text.literal("§cPlasma beam deactivated"), true);
            return;
        }

        // Activate beam
        beamStates.put(uuid, new BeamState(true, BEAM_DURATION_TICKS));
        player.sendMessage(Text.literal("§aPlasma beam activated!"), true);
    }

    public static void tickBeam(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        BeamState state = beamStates.get(uuid);

        if (state == null || !state.isActive) return;

        state.ticksRemaining--;

        if (state.ticksRemaining <= 0) {
            state.isActive = false;
            beamStates.remove(uuid);
            player.sendMessage(Text.literal("§cPlasma beam depleted"), true);
            return;
        }

        // Fire beam in look direction
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector();
        Vec3d end = start.add(direction.multiply(20));

        // Find entities in beam path
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(20),
                e -> e != player
        );

        for (LivingEntity entity : entities) {
            Vec3d entityPos = entity.getEyePos();
            double distanceToBeam = distanceToLine(entityPos, start, end);

            if (distanceToBeam < 0.5) {
                // Hit! Deal damage (half a heart = 1.0 damage)
                entity.damage(player.getEntityWorld().toServerWorld(),
                        player.getDamageSources().magic(), 1.0f);
                entity.setOnFireFor(1);
            }
        }

        // Spawn beam particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 2 == 0) {
            for (int i = 0; i < 20; i++) {
                double progress = i / 20.0;
                Vec3d particlePos = start.add(direction.multiply(20 * progress));

                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0);

                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    private static double distanceToLine(Vec3d point, Vec3d lineStart, Vec3d lineEnd) {
        Vec3d line = lineEnd.subtract(lineStart);
        Vec3d pointToStart = point.subtract(lineStart);

        double lineLength = line.length();
        if (lineLength == 0) return pointToStart.length();

        double t = Math.max(0, Math.min(1, pointToStart.dotProduct(line) / (lineLength * lineLength)));
        Vec3d projection = lineStart.add(line.multiply(t));

        return point.distanceTo(projection);
    }

    private static class BeamState {
        boolean isActive;
        int ticksRemaining;

        BeamState(boolean isActive, int ticksRemaining) {
            this.isActive = isActive;
            this.ticksRemaining = ticksRemaining;
        }
    }
}

