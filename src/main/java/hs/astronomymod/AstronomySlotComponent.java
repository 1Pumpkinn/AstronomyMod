package hs.astronomymod;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AstronomySlotComponent {
    private static final Map<UUID, AstronomySlotComponent> PLAYER_COMPONENTS = new HashMap<>();

    private ItemStack astronomyStack = ItemStack.EMPTY;
    private int cooldown = 0;

    public static AstronomySlotComponent get(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        AstronomySlotComponent component = PLAYER_COMPONENTS.get(uuid);

        if (component == null) {
            component = new AstronomySlotComponent();
            PLAYER_COMPONENTS.put(uuid, component);
        }

        return component;
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