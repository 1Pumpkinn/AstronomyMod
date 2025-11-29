package hs.astronomymod.effect;

import hs.astronomymod.AstronomyMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModStatusEffects {
    public static final RegistryKey<StatusEffect> STUN_KEY = RegistryKey.of(
            RegistryKeys.STATUS_EFFECT,
            Identifier.of(AstronomyMod.MOD_ID, "stun")
    );

    public static final StatusEffect STUN = Registry.register(
            Registries.STATUS_EFFECT,
            STUN_KEY,
            new StunStatusEffect()
    );

    public static final RegistryKey<StatusEffect> HIDDEN_HEARTS_KEY = RegistryKey.of(
            RegistryKeys.STATUS_EFFECT,
            Identifier.of(AstronomyMod.MOD_ID, "hidden_hearts")
    );

    public static final StatusEffect HIDDEN_HEARTS = Registry.register(
            Registries.STATUS_EFFECT,
            HIDDEN_HEARTS_KEY,
            new HiddenHeartsStatusEffect()
    );

    public static RegistryEntry<StatusEffect> getStunEntry() {
        try {
            return Registries.STATUS_EFFECT.getOrThrow(STUN_KEY);
        } catch (Exception e) {
            AstronomyMod.LOGGER.error("Failed to get STUN status effect entry", e);
            return null;
        }
    }

    public static RegistryEntry<StatusEffect> getHiddenHeartsEntry() {
        try {
            return Registries.STATUS_EFFECT.getOrThrow(HIDDEN_HEARTS_KEY);
        } catch (Exception e) {
            AstronomyMod.LOGGER.error("Failed to get HIDDEN_HEARTS status effect entry", e);
            return null;
        }
    }

    public static void registerStatusEffects() {
        AstronomyMod.LOGGER.info("Registering Astronomy Mod Status Effects");
    }
}