package hs.astronomymod.item.custom;

import hs.astronomymod.item.AstronomyItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class PulsarItem extends AstronomyItem {
    private int pulseCounter = 0;

    public PulsarItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        pulseCounter++;

        // Passive: Rhythmic Pulses - periodic powerful buffs
        int cycle = pulseCounter % 60;

        if (cycle == 0) {
            // Energy pulse every 3 seconds
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 2, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 2, false, false, false));

            // Heal on pulse
            player.heal(2.0f);
        }

        // Constant jump boost
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, 2, false, false, true));

        // Electromagnetic sight - see entities through walls
        if (pulseCounter % 40 == 0) {
            List<net.minecraft.entity.LivingEntity> distantEntities = player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class,
                    player.getBoundingBox().expand(40),
                    e -> e != player
            );
            distantEntities.forEach(entity -> {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0));
            });
        }

        // Dodge chance - periodic resistance
        if (cycle >= 15 && cycle <= 25) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20, 1, false, false, true));
        }

        // Rotating beam particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 2 == 0) {
            Vec3d pos = player.getEntityPos();
            double angle = player.age * 0.2;

            // Two rotating beams
            for (int beam = 0; beam < 2; beam++) {
                double beamAngle = angle + (beam * Math.PI);
                for (int i = 0; i < 5; i++) {
                    double distance = i * 0.8;
                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                            pos.x + Math.cos(beamAngle) * distance,
                            pos.y + 1,
                            pos.z + Math.sin(beamAngle) * distance,
                            1, 0, 0, 0, 0);
                }
            }

            // Central glow
            if (cycle == 0) {
                for (int i = 0; i < 20; i++) {
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x, pos.y + 1, pos.z,
                            1,
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5,
                            (Math.random() - 0.5) * 0.5, 0.1);
                }
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Electromagnetic Superpulse - devastating area control
        Vec3d playerPos = player.getEntityPos();

        // Three expanding pulses
        for (int pulse = 0; pulse < 3; pulse++) {
            double pulseRadius = 15 + pulse * 5;

            List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.LivingEntity.class,
                    player.getBoundingBox().expand(pulseRadius),
                    e -> e != player && e.getEntityPos().distanceTo(playerPos) <= pulseRadius
            );

            entities.forEach(entity -> {
                double distance = entity.getEntityPos().distanceTo(playerPos);

                // Damage falloff
                float damage = (float) (6.0 - distance * 0.2);
                entity.damage(player.getEntityWorld().toServerWorld(),
                        player.getDamageSources().magic(), Math.max(damage, 2.0f));

                // Complete sensory overload
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 2));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 150, 3));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 150, 2));

                // Mark all enemies
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));

                // Electromagnetic interference - mining fatigue
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 150, 3));
            });
        }

        // Player becomes hypercharged
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 4, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 300, 4, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 300, 4, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 2, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, false, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 1, false, false, true));

        // EMP burst effects
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
                    1.5f, 1.5f);
            serverWorld.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS,
                    2.0f, 2.0f);

            Vec3d pos = player.getEntityPos();

            // Expanding electromagnetic rings
            for (int ring = 0; ring < 5; ring++) {
                double ringRadius = 5 + ring * 6;
                for (int i = 0; i < 120; i++) {
                    double angle = (i / 120.0) * Math.PI * 2;
                    double heightVar = Math.sin(angle * 8) * 2;
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + Math.cos(angle) * ringRadius,
                            pos.y + 1 + heightVar,
                            pos.z + Math.sin(angle) * ringRadius,
                            1, 0, 0.1, 0, 0.2);
                }
            }

            // Vertical beams
            for (int i = 0; i < 50; i++) {
                double angle = (i / 50.0) * Math.PI * 2;
                for (int height = 0; height < 8; height++) {
                    serverWorld.spawnParticles(ParticleTypes.END_ROD,
                            pos.x + Math.cos(angle) * 3,
                            pos.y + height * 0.5,
                            pos.z + Math.sin(angle) * 3,
                            1, 0, 0, 0, 0);
                }
            }

            // Energy sphere
            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double vertAngle = Math.random() * Math.PI;
                double radius = 2;
                serverWorld.spawnParticles(ParticleTypes.FIREWORK,
                        pos.x + Math.cos(angle) * Math.sin(vertAngle) * radius,
                        pos.y + 1 + Math.cos(vertAngle) * radius,
                        pos.z + Math.sin(angle) * Math.sin(vertAngle) * radius,
                        1, 0, 0, 0, 0);
            }

            // Lightning strikes
            for (int i = 0; i < 8; i++) {
                double angle = (i / 8.0) * Math.PI * 2;
                for (int j = 0; j < 15; j++) {
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + Math.cos(angle) * 8,
                            pos.y + 8 - j * 0.5,
                            pos.z + Math.sin(angle) * 8,
                            2, 0.1, 0.1, 0.1, 0.3);
                }
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§3Passive: Rhythmic Pulses"));
        tooltip.accept(Text.literal("  §7• Periodic Speed & Haste"));
        tooltip.accept(Text.literal("  §7• Enhanced Jump"));
        tooltip.accept(Text.literal("  §7• Electromagnetic Sight"));
        tooltip.accept(Text.literal("  §7• Energy Regeneration"));
        tooltip.accept(Text.literal("§bActive: EM Superpulse"));
        tooltip.accept(Text.literal("  §7• Area Sensory Overload"));
        tooltip.accept(Text.literal("  §7• Complete Disorientation"));
        tooltip.accept(Text.literal("§e+ Hypercharge Mode"));
        tooltip.accept(Text.literal("§e+ Extreme Mobility"));
    }
}