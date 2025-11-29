package hs.astronomymod.abilities.neutronstar;

import hs.astronomymod.abilities.Ability;
import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import hs.astronomymod.abilities.neutronstar.active.GravitationalShockwaveActive;
import hs.astronomymod.abilities.neutronstar.active.PlasmaBeamActive;
import hs.astronomymod.abilities.neutronstar.passive.KnockbackImmunityPassive;
import hs.astronomymod.abilities.neutronstar.passive.ProjectileDeflectionPassive;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class NeutronStarAbility implements Ability {

    private final List<PassiveAbilityComponent> passiveAbilities = List.of(
            new KnockbackImmunityPassive(),
            new ProjectileDeflectionPassive()
    );

    private final ActiveAbilityComponent primaryActive = new GravitationalShockwaveActive();
    private final ActiveAbilityComponent secondaryActive = new PlasmaBeamActive();

    @Override
    public void applyPassive(ServerPlayerEntity player) {
        int shards = getShardCount(player);

        passiveAbilities.stream()
                .filter(passive -> shards >= passive.getRequiredShards())
                .forEach(passive -> passive.apply(player));

        // Handle plasma beam ticking
        PlasmaBeamActive.tickBeam(player);

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
        if (player.age % 15 != 0) return;

        Vec3d pos = player.getEntityPos();
        for (int i = 0; i < 3; i++) {
            serverWorld.spawnParticles(ParticleTypes.FIREWORK,
                    pos.x + (Math.random() - 0.5) * 0.5,
                    pos.y + 0.5 + Math.random() * 1.5,
                    pos.z + (Math.random() - 0.5) * 0.5,
                    1, 0, 0, 0, 0);
        }
    }
}
