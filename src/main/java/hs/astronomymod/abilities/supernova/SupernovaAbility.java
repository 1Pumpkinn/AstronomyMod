package hs.astronomymod.abilities.supernova;

import hs.astronomymod.abilities.Ability;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public class SupernovaAbility implements Ability {
    @Override
    public void applyPassive(ServerPlayerEntity player) {
        // Example: fire immunity, night vision, etc.
    }

    @Override
    public void applyActive(ServerPlayerEntity player) {
        // Example: massive explosion, knockback
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // Client-side effects if needed
    }
}
