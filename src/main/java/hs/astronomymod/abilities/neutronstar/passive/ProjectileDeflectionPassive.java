package hs.astronomymod.abilities.neutronstar.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ProjectileDeflectionPassive implements PassiveAbilityComponent {
    @Override
    public int getRequiredShards() {
        return 2;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        List<ProjectileEntity> projectiles = player.getEntityWorld().getEntitiesByClass(
                ProjectileEntity.class,
                player.getBoundingBox().expand(3),
                p -> p.getOwner() != player
        );

        projectiles.forEach(projectile -> {
            Vec3d pPos = projectile.getEntityPos();
            Vec3d playerPos = player.getEntityPos();
            Vec3d direction = pPos.subtract(playerPos).normalize();

            // Bend projectile away and reduce velocity
            projectile.setVelocity(direction.multiply(0.3));
            projectile.velocityModified = true;
        });
    }
}

