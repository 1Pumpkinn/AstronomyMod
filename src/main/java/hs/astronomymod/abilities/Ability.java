package hs.astronomymod.abilities;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public interface Ability {
    void applyPassive(ServerPlayerEntity player);
    void applyActive(ServerPlayerEntity player);
    void applyPassiveClient(ClientPlayerEntity player);
}
