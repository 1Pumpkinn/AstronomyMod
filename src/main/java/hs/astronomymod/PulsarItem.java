package hs.astronomymod;

public class PulsarItem extends AstronomyItem {
    public PulsarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyPassiveAbility(ServerPlayer player) {
        // Passive: Energy pulses - periodic speed boost
        if (player.tickCount % 60 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, false));
        }

        // Upside 1: Jump boost
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false, true));

        // Pulse particles
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 20 == 0) {
            Vec3 pos = player.position();
            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2 * i) / 10;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        pos.x + Math.cos(angle) * 2,
                        pos.y + 1,
                        pos.z + Math.sin(angle) * 2,
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayer player) {
        // Active: Electromagnetic Pulse - blind and confuse enemies
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(12),
                e -> e != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        });

        // Upside 2: Glowing to see through walls
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(30),
                e -> e != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        });

        // Burst effect
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();
            for (int i = 0; i < 40; i++) {
                double angle = Math.random() * Math.PI * 2;
                double pitch = Math.random() * Math.PI - Math.PI / 2;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y + 1, pos.z,
                        1,
                        Math.cos(angle) * Math.cos(pitch) * 3,
                        Math.sin(pitch) * 3,
                        Math.sin(angle) * Math.cos(pitch) * 3,
                        0.3);
            }
        }
    }

    @Override
    protected void addCustomTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.literal("§3Passive: Energy Pulses"));
        tooltipComponents.add(Component.literal("§bActive: EMP Burst"));
        tooltipComponents.add(Component.literal("§e+ Jump Boost"));
        tooltipComponents.add(Component.literal("§e+ Enemy Detection"));
    }
}
