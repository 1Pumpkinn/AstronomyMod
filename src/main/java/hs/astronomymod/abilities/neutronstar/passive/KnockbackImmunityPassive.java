package hs.astronomymod.abilities.neutronstar.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class KnockbackImmunityPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 1;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        if (player.getEntityWorld() instanceof ServerWorld) {
            // Reduce horizontal knockback by 80%
            player.setVelocity(player.getVelocity().multiply(0.8, 1.0, 0.8));
        }
    }
}

