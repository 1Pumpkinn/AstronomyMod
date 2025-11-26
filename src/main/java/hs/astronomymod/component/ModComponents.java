package hs.astronomymod.component;

import com.mojang.serialization.Codec;
import hs.astronomymod.AstronomyMod;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {
    // Component to store the number of shards (0-3)
    public static final ComponentType<Integer> ASTRONOMY_SHARDS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(AstronomyMod.MOD_ID, "astronomy_shards"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .build()
    );

    public static void registerComponents() {
        AstronomyMod.LOGGER.info("Registering Astronomy Mod Components");
    }
}