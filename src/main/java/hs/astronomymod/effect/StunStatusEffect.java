package hs.astronomymod.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class StunStatusEffect extends StatusEffect {
    public StunStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x1a1a1a); // Dark gray color
    }

    // Movement blocking is handled in AstronomyMod via tick event
    // This status effect just marks the entity as stunned
}

