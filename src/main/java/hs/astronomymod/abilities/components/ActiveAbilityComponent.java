package hs.astronomymod.abilities.components;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ActiveAbilityComponent {
    int getRequiredShards();
    void activate(ServerPlayerEntity player);
}

