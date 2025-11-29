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

/**
 * Plasma Beam active ability - fires a damaging beam in front of the player.
 * Deals continuous damage in a narrow path.
 */
public class PlasmaBeamActive implements ActiveAbilityComponent {
    /** Duration the beam remains active (server ticks) */
    private static final int BEAM_DURATION = 200;      // 10 seconds
    /** Beam max range in blocks */
    private static final double BEAM_RANGE = 25.0;
    /** Damage per tick */
    private static final float BEAM_DAMAGE = 1.0F;     // Half a heart per tick
    /** Width in blocks the beam extends (total width = width * 2+ beam) */
    private static final double BEAM_WIDTH = 0.3;
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
    /**
     * Ticked by server event. Handles the beam's logic and duration.
     */
    public static void tickBeam(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        BeamState state = ACTIVE_BEAMS.get(id);
        if (state == null || !state.active) return;
        state.time--;
        if (state.time <= 0) {
            return;
        }
        runBeamLogic(player);
    }
    /**
     * Core plasma beam logic. Handles damage and visual effects.
     */
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
            world.spawnParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
    /**
     * Damages entities within the beam's path.
     * Uses a narrow beam hitbox, set by BEAM_WIDTH for accuracy.
     */
    private static void damageBeamTargets(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d end) {
        // Beam as a thin box (hitbox width):
        Box beamBox = new Box(start, end).expand(BEAM_WIDTH);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, beamBox,
                e -> e != player && e.isAlive());
        for (LivingEntity target : entities) {
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