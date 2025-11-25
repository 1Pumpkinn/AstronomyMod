package hs.astronomymod;

public class NeutronStarItem extends AstronomyItem {
    public NeutronStarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void applyPassiveAbility(ServerPlayer player) {
        // Passive: Extreme density - knockback resistance
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false, false));

        // Upside 1: Increased attack damage
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false, true));
    }

    @Override
    public void applyActiveAbility(ServerPlayer player) {
        // Active: Magnetic Field - stun nearby enemies
        player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(7),
                e -> e != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 10));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        });

        // Upside 2: Haste boost
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 2, false, false, true));
    }

    @Override
    protected void addCustomTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(Component.literal("§7Passive: Dense Armor"));
        tooltipComponents.add(Component.literal("§fActive: Magnetic Field"));
        tooltipComponents.add(Component.literal("§e+ Strength"));
        tooltipComponents.add(Component.literal("§e+ Haste when active"));
    }
}
