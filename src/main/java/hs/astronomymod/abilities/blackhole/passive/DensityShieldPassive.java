package hs.astronomymod.abilities.blackhole.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class DensityShieldPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 2;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        if (player.fallDistance > 0) {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 2, 4, false, false, false
            ));
        }
    }
}

