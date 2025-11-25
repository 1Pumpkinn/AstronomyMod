package hs.astronomymod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.RegistryWrapper;

public class AstronomySlotComponent {
    private ItemStack astronomyStack = ItemStack.EMPTY;
    private int cooldown = 0;

    public static AstronomySlotComponent get(ServerPlayerEntity player) {
        // This stores component data in player NBT
        NbtCompound nbt = player.getPersistentData();
        AstronomySlotComponent component = new AstronomySlotComponent();

        if (nbt.contains("AstronomySlot")) {
            NbtCompound slotData = nbt.getCompound("AstronomySlot");
            RegistryWrapper.WrapperLookup registryLookup = player.getRegistryManager();
            component.astronomyStack = ItemStack.fromNbt(registryLookup, slotData.getCompound("Item")).orElse(ItemStack.EMPTY);
            component.cooldown = slotData.getInt("Cooldown");
        }

        return component;
    }

    public void save(ServerPlayerEntity player) {
        NbtCompound nbt = player.getPersistentData();
        NbtCompound slotData = new NbtCompound();
        RegistryWrapper.WrapperLookup registryLookup = player.getRegistryManager();
        slotData.put("Item", astronomyStack.encode(registryLookup));
        slotData.putInt("Cooldown", cooldown);
        nbt.put("AstronomySlot", slotData);
    }

    public void tick(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbility(player);
        }

        if (cooldown > 0) {
            cooldown--;
        }
    }

    public void activateAbility(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem && cooldown <= 0) {
            astronomyItem.applyActiveAbility(player);
            cooldown = 600; // 30 second cooldown
        }
    }

    public ItemStack getAstronomyStack() {
        return astronomyStack;
    }

    public void setAstronomyStack(ItemStack stack) {
        this.astronomyStack = stack;
    }

    public int getCooldown() {
        return cooldown;
    }
}