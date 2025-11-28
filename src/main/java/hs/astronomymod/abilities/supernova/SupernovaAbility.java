package hs.astronomymod.abilities.supernova;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import hs.astronomymod.abilities.supernova.active.SolarDashActive;
import hs.astronomymod.abilities.supernova.active.SolarFlareActive;
import hs.astronomymod.abilities.supernova.passive.SolarEruptionPassive;
import hs.astronomymod.abilities.supernova.passive.StellarHeatPassive;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SupernovaAbility implements Ability {

    private final List<PassiveAbilityComponent> passiveAbilities = List.of(
            new StellarHeatPassive(),
            new SolarEruptionPassive()
    );

    private final ActiveAbilityComponent primaryActive = new SolarDashActive();
    private final ActiveAbilityComponent secondaryActive = new SolarFlareActive();

    @Override
    public void applyPassive(ServerPlayerEntity player) {
        int shards = getShardCount(player);

        passiveAbilities.stream()
                .filter(passive -> shards >= passive.getRequiredShards())
                .forEach(passive -> passive.apply(player));

        spawnPassiveParticles(player, shards);
    }

    @Override
    public boolean applyActive(ServerPlayerEntity player, AbilityActivation activation) {
        int shards = getShardCount(player);
        ActiveAbilityComponent ability = activation == AbilityActivation.SECONDARY ? secondaryActive : primaryActive;

        if (ability == null) return false;

        if (shards < ability.getRequiredShards()) {
            player.sendMessage(Text.translatable("message.astronomymod.need_shards", ability.getRequiredShards()), true);
            return false;
        }

        ability.activate(player);
        return true;
    }

    @Override
    public void applyPassiveClient(ClientPlayerEntity player) {
        // Client-side visual effects if needed
    }

    private int getShardCount(ServerPlayerEntity player) {
        var astronomyStack = hs.astronomymod.client.AstronomySlotComponent.get(player).getAstronomyStack();
        return astronomyStack.getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);
    }

    private void spawnPassiveParticles(ServerPlayerEntity player, int shards) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        if (player.age % 5 != 0) return;

        Vec3d pos = player.getEntityPos();

        for (int i = 0; i < shards; i++) {
            serverWorld.spawnParticles(ParticleTypes.FLAME,
                    pos.x + (Math.random() - 0.5) * 0.8,
                    pos.y + 0.5 + Math.random() * 1.5,
                    pos.z + (Math.random() - 0.5) * 0.8,
                    1, 0, 0, 0, 0.02);
        }
    }
}