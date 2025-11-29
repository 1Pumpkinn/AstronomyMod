package hs.astronomymod.abilities.planet.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.server.network.ServerPlayerEntity;

public class NoFallDamagePassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 1;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        if (player.fallDistance > 0) {
            player.fallDistance = 0;
        }
    }
}