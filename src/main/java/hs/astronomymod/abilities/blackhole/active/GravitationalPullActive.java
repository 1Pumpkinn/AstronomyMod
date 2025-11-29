package hs.astronomymod.abilities.blackhole.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GravitationalPullActive implements ActiveAbilityComponent {
    // Track players who have activated the stun ability
    private static final Map<UUID, Boolean> stunReady = new ConcurrentHashMap<>();

    @Override
    public int getRequiredShards() {
        return 0;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        // Mark that the next hit will stun
        stunReady.put(player.getUuid(), true);
        
        spawnAbilityEffects(player);
        
        player.sendMessage(net.minecraft.text.Text.literal("§5Next hit will stun the target!"), true);
    }

    public static boolean isStunReady(ServerPlayerEntity player) {
        return stunReady.getOrDefault(player.getUuid(), false);
    }

    public static void applyStun(ServerPlayerEntity attacker, LivingEntity target) {
        if (isStunReady(attacker)) {
            // Apply stun for 5 seconds (100 ticks)
            var stunEntry = hs.astronomymod.effect.ModStatusEffects.getStunEntry();
            if (stunEntry != null) {
                target.addStatusEffect(new StatusEffectInstance(
                        stunEntry, 100, 0, false, false, true
                ));
            }
            
            // Remove the ready flag
            stunReady.remove(attacker.getUuid());
            
            // Visual feedback
            if (target.getEntityWorld() instanceof ServerWorld serverWorld) {
                Vec3d pos = target.getEntityPos();
                serverWorld.playSound(null, pos.x, pos.y, pos.z,
                        SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0f, 0.8f);
                
                for (int i = 0; i < 20; i++) {
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                            pos.x + (Math.random() - 0.5) * 2,
                            pos.y + 1 + Math.random() * 2,
                            pos.z + (Math.random() - 0.5) * 2,
                            2, 0.1, 0.1, 0.1, 0.1);
                }
            }
        }
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
