package hs.astronomymod.abilities.components;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PassiveAbilityComponent {
    int getRequiredShards();
    void apply(ServerPlayerEntity player);
}

