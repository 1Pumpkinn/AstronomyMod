package hs.astronomymod.abilities.neutronstar.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlasmaBeamActive implements ActiveAbilityComponent {
    private static final int BEAM_DURATION = 200;      // 10 seconds
    private static final double BEAM_RANGE = 25.0;     // Distance
    private static final float BEAM_DAMAGE = 1.0F;     // half heart per tick
    private static final Map<UUID, BeamState> ACTIVE_BEAMS = new ConcurrentHashMap<>();

    @Override
    public int getRequiredShards() {
        return 3;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        UUID id = player.getUuid();

        BeamState state = ACTIVE_BEAMS.get(id);
        if (state != null && state.active) {
            state.active = false;
            ACTIVE_BEAMS.remove(id);
            player.sendMessage(Text.literal("§cPlasma Beam Disabled"), true);
            return;
        }

        ACTIVE_BEAMS.put(id, new BeamState(true, BEAM_DURATION));
        player.sendMessage(Text.literal("§aPlasma Beam Activated"), true);
    }

    /** Tick from a global server-tick event */
    public static void tickBeam(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        BeamState state = ACTIVE_BEAMS.get(id);

        if (state == null || !state.active) return;

        state.time--;

        if (state.time <= 0) {
            ACTIVE_BEAMS.remove(id);
            return;
        }

        runBeamLogic(player);
    }

    private static void runBeamLogic(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld().toServerWorld();

        Vec3d eye = player.getCameraPosVec(1f);
        Vec3d look = player.getRotationVec(1f);
        Vec3d end = eye.add(look.multiply(BEAM_RANGE));

        // Draw particles along beam
        drawBeamParticles(world, eye, end);

        // Damage entities along beam
        damageBeamTargets(world, player, eye, end);
    }

    private static void drawBeamParticles(ServerWorld world, Vec3d start, Vec3d end) {
        double dist = start.distanceTo(end);
        int steps = (int) (dist * 4);

        for (int i = 0; i < steps; i++) {
            double t = i / (double) steps;
            Vec3d pos = start.lerp(end, t);
            world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private static void damageBeamTargets(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d end) {
        // Beam as a thin box
        Box beamBox = new Box(start, end).expand(0.8);

        // Get all living entities in the beam area
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, beamBox,
                e -> e != player && e.isAlive());

        for (LivingEntity target : entities) {
            // Approximate intersection: if bounding box intersects the beam box
            if (target.getBoundingBox().intersects(beamBox)) {
                target.damage(world, world.getDamageSources().playerAttack(player), BEAM_DAMAGE);
            }
        }
    }

    private static class BeamState {
        boolean active;
        int time;

        BeamState(boolean active, int time) {
            this.active = active;
            this.time = time;
        }
    }
}
