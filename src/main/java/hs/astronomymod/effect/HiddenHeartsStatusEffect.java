package hs.astronomymod.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class HiddenHeartsStatusEffect extends StatusEffect {
    public HiddenHeartsStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0x5a1a5a); // Purple color to match the glow in the image
    }

    // The actual hiding of hearts/hunger is handled client-side via mixin
    // This status effect serves as a marker
}

