package hs.astronomymod;

public class PlanetItem extends AstronomyItem {
    public PlanetItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyPassiveAbility(ServerPlayer player) {
        // Passive: Increased gravity resistance (slow falling effect)
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false, false));

        // Upside 1: Regeneration
        if (player.tickCount % 60 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
        }
    }

    @Override
    public void applyActiveAbility(ServerPlayer player) {
        // Active: Gravitational Pull - pulls nearby entities (simulated with slow)
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(10),
                e -> e != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            // Upside 2: Damage resistance when using active ability
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, false, true));
        });
    }

    @Override
    protected void addCustomTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.literal("§bPassive: Gravity Control"));
        tooltipComponents.add(Component.literal("§aActive: Gravitational Pull"));
        tooltipComponents.add(Component.literal("§e+ Regeneration"));
        tooltipComponents.add(Component.literal("§e+ Resistance when active"));
    }
}
