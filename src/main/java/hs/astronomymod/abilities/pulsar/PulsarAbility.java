package hs.astronomymod.abilities.pulsar;

import hs.astronomymod.abilities.Ability;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public class PulsarAbility implements Ability {
    @Override
    public void applyPassive(ServerPlayerEntity player) {
        // Example: periodic speed, jump boost, energy regen
    }

    @Override
    public void applyActive(ServerPlayerEntity player) {
        // Example: EM superpulse effects
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // Client-side effects if needed
    }
}
