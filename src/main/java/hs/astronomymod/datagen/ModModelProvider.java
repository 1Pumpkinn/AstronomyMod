package hs.astronomymod.datagen;

import hs.astronomymod.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // Add block models here when you have blocks
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // Generate basic item models
        //itemModelGenerator.register(ModItems.PLANET_ITEM, Models.GENERATED);
        itemModelGenerator.register(ModItems.SUPERNOVA_ITEM, Models.GENERATED);
        itemModelGenerator.register(ModItems.BLACKHOLE_ITEM, Models.GENERATED);
        itemModelGenerator.register(ModItems.NEUTRON_STAR_ITEM, Models.GENERATED);
        itemModelGenerator.register(ModItems.PULSAR_ITEM, Models.GENERATED);
        itemModelGenerator.register(ModItems.ASTRONOMY_SHARD, Models.GENERATED);
    }
}