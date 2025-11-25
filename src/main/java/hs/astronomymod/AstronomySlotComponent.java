package hs.astronomymod;

public class AstronomySlotComponent {
    private ItemStack astronomyStack = ItemStack.EMPTY;
    private int cooldown = 0;

    public static AstronomySlotComponent get(ServerPlayer player) {
        // This would normally use a component attachment system
        // For now, we'll store in player NBT
        CompoundTag nbt = player.getPersistentData();
        AstronomySlotComponent component = new AstronomySlotComponent();

        if (nbt.contains("AstronomySlot")) {
            CompoundTag slotData = nbt.getCompound("AstronomySlot");
            component.astronomyStack = ItemStack.parseOptional(player.registryAccess(), slotData.getCompound("Item"));
            component.cooldown = slotData.getInt("Cooldown");
        }

        return component;
    }

    public void save(ServerPlayer player) {
        CompoundTag nbt = player.getPersistentData();
        CompoundTag slotData = new CompoundTag();
        slotData.put("Item", astronomyStack.save(player.registryAccess()));
        slotData.putInt("Cooldown", cooldown);
        nbt.put("AstronomySlot", slotData);
    }

    public void tick(ServerPlayer player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbility(player);
        }

        if (cooldown > 0) {
            cooldown--;
        }
    }

    public void activateAbility(ServerPlayer player) {
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
