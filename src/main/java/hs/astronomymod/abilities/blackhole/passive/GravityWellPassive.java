package hs.astronomymod.abilities.blackhole.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class GravityWellPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 1;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        List<LivingEntity> nearbyEntities = player.getEntityWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(6),
                e -> e != player
        );

        nearbyEntities.forEach(entity -> entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 40, 2, false, false, true
        )));
    }
}

