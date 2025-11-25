package hs.astronomymod;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Properties;

public class BlackholeItem extends AstronomyItem {
    public BlackholeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyPassiveAbility(ServerPlayerEntity player) {
        // Passive: Void protection - absorbs damage
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 1, false, false, false));

        // Upside 1: Increased max health
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 40, 2, false, false, true));

        // Dark particles
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 10 == 0) {
            Vec3 pos = player.position();
            serverLevel.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1, pos.z, 3, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayer player) {
        // Active: Gravity Well - pull entities and deal damage
        Vec3 playerPos = player.position();
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(15),
                e -> e != player).forEach(entity -> {
            Vec3 entityPos = entity.position();
            Vec3 direction = playerPos.subtract(entityPos).normalize();
            entity.setDeltaMovement(direction.scale(0.5));
            entity.hurt(player.damageSources().magic(), 4.0f);
        });

        // Upside 2: Invisibility during active ability
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));

        // Vortex particles
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = 5 + Math.random() * 5;
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.x + Math.cos(angle) * radius,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * radius,
                        1, 0, 0, 0, 0.2);
            }
        }
    }

    @Override
    protected void addCustomTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.literal("§5Passive: Void Protection"));
        tooltipComponents.add(Component.literal("§8Active: Gravity Well"));
        tooltipComponents.add(Component.literal("§e+ Health Boost"));
        tooltipComponents.add(Component.literal("§e+ Invisibility when active"));
    }
}
