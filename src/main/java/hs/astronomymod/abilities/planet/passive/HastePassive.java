package hs.astronomymod.abilities.planet.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class HastePassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 2;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.HASTE, 40, 0, false, false, true
        ));
    }
}

