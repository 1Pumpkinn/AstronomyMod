package hs.astronomymod.abilities.supernova.passive;

import hs.astronomymod.abilities.components.PassiveAbilityComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class StellarHeatPassive implements PassiveAbilityComponent {
    private static final Identifier FIRE_ASPECT_ID = Identifier.of("astronomymod", "fire_aspect");

    @Override
    public int getRequiredShards() {
        return 1;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        var attackDamage = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (attackDamage != null && !attackDamage.hasModifier(FIRE_ASPECT_ID)) {
            attackDamage.addTemporaryModifier(
                    new EntityAttributeModifier(
                            FIRE_ASPECT_ID,
                            0.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );
        }

        if (player.age % 20 == 0 && player.getAttacking() instanceof LivingEntity target) {
            target.setOnFireFor(4);
        }
    }
}