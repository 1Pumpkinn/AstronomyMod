package hs.astronomymod.abilities.blackhole.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
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

/**
 * Singularity Collapse: Pulls nearby entities toward the player and afflicts them with strong debuffs.
 * Player receives powerful buffs for a short duration.
 */
public class SingularityCollapseActive implements ActiveAbilityComponent {
    private static final double COLLAPSE_RADIUS = 6.0;
    private static final double PULL_STRENGTH = 2.5;
    private static final int DEBUFF_DURATION = 200;
    private static final int PLAYER_BUFF_DURATION = 140;
    @Override
    public int getRequiredShards() {
        return 3;
    }
    @Override
    public void activate(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEntityPos();
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(COLLAPSE_RADIUS),
                e -> e != player
        );
        if (entities.isEmpty()) {
            player.sendMessage(net.minecraft.text.Text.literal("§7No valid targets for Singularity Collapse!"), true);
        }
        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos);
            direction = direction.lengthSquared() > 1e-5 ? direction.normalize() : new Vec3d(0,1,0);
            entity.setVelocity(direction.multiply(PULL_STRENGTH));
            entity.velocityModified = true;
            // Hide hearts and hunger with custom status effect
            var hiddenHeartsEntry = hs.astronomymod.effect.ModStatusEffects.getHiddenHeartsEntry();
            if (hiddenHeartsEntry != null) {
                entity.addStatusEffect(new StatusEffectInstance(hiddenHeartsEntry, DEBUFF_DURATION, 0, false, false, true));
            }
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, DEBUFF_DURATION, 0, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, DEBUFF_DURATION, 0, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, DEBUFF_DURATION, 5, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, DEBUFF_DURATION, 3, false, false, true));
            float damage = 8.0f;
            entity.damage(player.getEntityWorld().toServerWorld(), player.getDamageSources().magic(), damage);
        });
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, PLAYER_BUFF_DURATION, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, PLAYER_BUFF_DURATION, 3, false, false, true));
        spawnAbilityEffects(player);
    }
    private void spawnAbilityEffects(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        Vec3d pos = player.getEntityPos();
        serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.5f, 0.5f);
        for (int layer = 0; layer < 5; layer++) {
            double layerRadius = 15 - layer * 3;
            for (int i = 0; i < 100; i++) {
                double angle = (i / 100.0) * Math.PI * 2 + layer * 0.5;
                double height = layer * 0.8;
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x + Math.cos(angle) * layerRadius,
                        pos.y + height,
                        pos.z + Math.sin(angle) * layerRadius,
                        1, 0, 0, 0, 0.3);
            }
        }
        for (int i = 0; i < 50; i++) {
            serverWorld.spawnParticles(ParticleTypes.SQUID_INK,
                    pos.x, pos.y + 1, pos.z,
                    2,
                    Math.random() - 0.5,
                    Math.random() - 0.5,
                    Math.random() - 0.5, 0.1);
        }
    }
}

