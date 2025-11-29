package hs.astronomymod.abilities.blackhole.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.server.network.ServerPlayerEntity;

public class DensityShieldPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 2;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        // Mace damage protection is handled via damage event listener in AstronomyMod
        // This passive just needs to be active (checked by shard count)
    }
}

