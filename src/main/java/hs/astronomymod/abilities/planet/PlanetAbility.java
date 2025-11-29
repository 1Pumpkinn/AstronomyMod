package hs.astronomymod.abilities.planet;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import hs.astronomymod.abilities.planet.active.GravityFlingActive;
import hs.astronomymod.abilities.planet.active.LightningStrikeActive;
import hs.astronomymod.abilities.planet.passive.HastePassive;
import hs.astronomymod.abilities.planet.passive.NoFallDamagePassive;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PlanetAbility implements Ability {

    private final List<PassiveAbilityComponent> passiveAbilities = List.of(
            new NoFallDamagePassive(),
            new HastePassive()
    );

    private final ActiveAbilityComponent primaryActive = new LightningStrikeActive();
    private final ActiveAbilityComponent secondaryActive = new GravityFlingActive();

    @Override
    public void applyPassive(ServerPlayerEntity player) {
        int shards = getShardCount(player);

        passiveAbilities.stream()
                .filter(passive -> shards >= passive.getRequiredShards())
                .forEach(passive -> passive.apply(player));

        // Handle fling state ticking
        GravityFlingActive.tickFlingState(player);

        spawnPassiveParticles(player);
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

    private void spawnPassiveParticles(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        if (player.age % 10 != 0) return;

        Vec3d pos = player.getEntityPos();
        for (int i = 0; i < 3; i++) {
            double angle = (player.age + i * 120) * 0.05;
            double radius = 1.5;
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    pos.x + Math.cos(angle) * radius,
                    pos.y + 1,
                    pos.z + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
    }
}