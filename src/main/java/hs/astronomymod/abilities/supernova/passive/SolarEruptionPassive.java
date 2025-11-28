package hs.astronomymod.abilities.supernova.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SolarEruptionPassive implements PassiveAbilityComponent {
    private static final int HEALTH_THRESHOLD = 6;
    private static final int COOLDOWN_TICKS = 300;

    private final Map<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();

    @Override
    public int getRequiredShards() {
        return 2;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        PlayerState state = playerStates.computeIfAbsent(player.getUuid(), id -> new PlayerState());

        if (state.cooldown > 0) {
            state.cooldown--;
        }
        if (player.getHealth() > HEALTH_THRESHOLD) {
            state.hasTriggeredEruption = false;
        }

        if (player.getHealth() <= HEALTH_THRESHOLD && !state.hasTriggeredEruption && state.cooldown <= 0) {
            triggerSolarEruption(player);
            state.hasTriggeredEruption = true;
            state.cooldown = COOLDOWN_TICKS;
        }
    }

    private void triggerSolarEruption(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 4, false, false, true
        ));
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 100, 0, false, false, true
        ));

        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        entities.forEach(entity -> {
            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.BLINDNESS, 100, 0, false, false, true
            ));
            entity.setOnFireFor(5);
            entity.damage(player.getEntityWorld().toServerWorld(),
                    player.getDamageSources().magic(), 4.0f);
        });

        spawnEruptionEffects(player, playerPos);
    }

    private void spawnEruptionEffects(ServerPlayerEntity player, Vec3d pos) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.5f, 1.0f);

        for (int ring = 0; ring < 3; ring++) {
            double radius = 2 + ring * 2;
            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 2;
                serverWorld.spawnParticles(ParticleTypes.FLAME,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 0.5,
                        pos.z + Math.sin(angle) * radius,
                        2, 0.2, 0.3, 0.2, 0.1);
            }
        }

        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * Math.PI * 2;
            double vert = Math.random() * Math.PI;
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x, pos.y + 1, pos.z,
                    1,
                    Math.cos(angle) * Math.sin(vert) * 0.5,
                    Math.cos(vert) * 0.5,
                    Math.sin(angle) * Math.sin(vert) * 0.5,
                    0);
        }
    }

    private static class PlayerState {
        private boolean hasTriggeredEruption = false;
        private int cooldown = 0;
    }
}

