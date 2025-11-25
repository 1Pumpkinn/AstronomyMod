package hs.astronomymod;

public class SupernovaItem extends AstronomyItem {
    public SupernovaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyPassiveAbility(ServerPlayer player) {
        // Passive: Fire Resistance
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));

        // Upside 1: Night Vision
        if (player.tickCount % 220 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, true));
        }

        // Particle effects
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 5 == 0) {
            Vec3 pos = player.position();
            serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1, pos.z, 2, 0.3, 0.5, 0.3, 0.01);
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayer player) {
        // Active: Explosive Burst - damages nearby entities
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(8),
                e -> e != player).forEach(entity -> {
            entity.hurt(player.damageSources().explosion(player, player), 8.0f);
            entity.setRemainingFireTicks(100);
        });

        // Upside 2: Speed boost after explosion
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2, false, false, true));

        // Visual effect
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();
            for (int i = 0; i < 50; i++) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        pos.x, pos.y + 1, pos.z, 1,
                        Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2, 0.5);
            }
        }
    }

    @Override
    protected void addCustomTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.literal("§cPassive: Fire Immunity"));
        tooltipComponents.add(Component.literal("§6Active: Explosive Burst"));
        tooltipComponents.add(Component.literal("§e+ Night Vision"));
        tooltipComponents.add(Component.literal("§e+ Speed after explosion"));
    }
}
