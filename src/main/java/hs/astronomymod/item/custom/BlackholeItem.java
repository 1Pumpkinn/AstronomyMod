package hs.astronomymod.item.custom;

import hs.astronomymod.AstronomyItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

public class BlackholeItem extends AstronomyItem {
    public BlackholeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Void protection - absorbs damage
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 40, 1, false, false, false));

        // Upside 1: Increased max health
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 40, 2, false, false, true));

        // Dark particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld && player.age % 10 == 0) {
            Vec3d pos = player.getEntityPos();
            serverWorld.spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1, pos.z, 3, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayerEntity player) {
        // Active: Gravity Well - pull entities and deal damage
        Vec3d playerPos = player.getEntityPos();
        List<net.minecraft.entity.LivingEntity> entities = player.getEntityWorld().getEntitiesByClass(
                net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(15),
                e -> e != player
        );
        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            Vec3d direction = playerPos.subtract(entityPos).normalize();
            entity.setVelocity(direction.multiply(0.5));
            entity.damage(player.getEntityWorld().toServerWorld(), player.getDamageSources().magic(), 4.0f);
        });

        // Upside 2: Invisibility during active ability
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0, false, false, true));

        // Vortex particles
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            Vec3d pos = player.getEntityPos();
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = 5 + Math.random() * 5;
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0.2);
            }
        }
    }

    @Override
    protected void addCustomTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal("§5Passive: Void Protection"));
        tooltip.accept(Text.literal("§8Active: Gravity Well"));
        tooltip.accept(Text.literal("§e+ Health Boost"));
        tooltip.accept(Text.literal("§e+ Invisibility when active"));
    }
}