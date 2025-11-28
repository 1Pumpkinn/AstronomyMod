package hs.astronomymod.abilities.blackhole;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import hs.astronomymod.abilities.blackhole.active.GravitationalPullActive;
import hs.astronomymod.abilities.blackhole.active.SingularityCollapseActive;
import hs.astronomymod.abilities.blackhole.passive.DensityShieldPassive;
import hs.astronomymod.abilities.blackhole.passive.GravityWellPassive;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BlackholeAbility implements Ability {

    private final List<PassiveAbilityComponent> passiveAbilities = List.of(
            new GravityWellPassive(),
            new DensityShieldPassive()
    );

    private final ActiveAbilityComponent primaryActive = new GravitationalPullActive();
    private final ActiveAbilityComponent secondaryActive = new SingularityCollapseActive();

    @Override
    public void applyPassive(ServerPlayerEntity player) {
        int shards = getShardCount(player);

        passiveAbilities.stream()
                .filter(passive -> shards >= passive.getRequiredShards())
                .forEach(passive -> passive.apply(player));

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
        if (player.age % 8 != 0) return;

        Vec3d pos = player.getEntityPos();

        for (int i = 0; i < 5; i++) {
            double angle = player.age * 0.15 + i * (Math.PI * 2 / 5);
            double radius = 1.5;

            serverWorld.spawnParticles(ParticleTypes.PORTAL,
                    pos.x + Math.cos(angle) * radius,
                    pos.y + 0.5 + Math.sin(angle * 2) * 0.5,
                    pos.z + Math.sin(angle) * radius,
                    2, 0.1, 0.1, 0.1, 0.05);

            serverWorld.spawnParticles(ParticleTypes.SQUID_INK,
                    pos.x + Math.cos(angle) * radius * 0.7,
                    pos.y + 1,
                    pos.z + Math.sin(angle) * radius * 0.7,
                    1, 0, 0, 0, 0);
        }
    }
}