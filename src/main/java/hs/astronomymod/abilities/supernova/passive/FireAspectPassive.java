package hs.astronomymod.abilities.supernova.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class FireAspectPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 1;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        if (player.age % 20 == 0 && player.getAttacking() instanceof LivingEntity target) {
            target.setOnFireFor(4);
        }
    }
}