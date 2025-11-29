package hs.astronomymod.abilities.planet.active;

import hs.astronomymod.abilities.components.ActiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class LightningStrikeActive implements ActiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 0;
    }

    @Override
    public void activate(ServerPlayerEntity player) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Vec3d playerPos = player.getEntityPos();

        List<LivingEntity> entities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(5),
                e -> e != player
        );

        entities.forEach(entity -> {
            Vec3d entityPos = entity.getEntityPos();
            
            // Strike with lightning effect
            net.minecraft.entity.LightningEntity lightning = new net.minecraft.entity.LightningEntity(
                    net.minecraft.entity.EntityType.LIGHTNING_BOLT, serverWorld);
            lightning.setPosition(entityPos);
            serverWorld.spawnEntity(lightning);
            
            // Deal 3 hearts (6 damage)
            entity.damage(serverWorld, player.getDamageSources().lightningBolt(), 6.0f);
        });

        spawnAbilityEffects(serverWorld, playerPos);
    }

    private void spawnAbilityEffects(ServerWorld serverWorld, Vec3d pos) {
        serverWorld.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.5f, 1.0f);

        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double radius = 2.5;
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + Math.cos(angle) * radius,
                    pos.y + 1,
                    pos.z + Math.sin(angle) * radius,
                    3, 0.2, 0.3, 0.2, 0.1);
        }
    }
}

