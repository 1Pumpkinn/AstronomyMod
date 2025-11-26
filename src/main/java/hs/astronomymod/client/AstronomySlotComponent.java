package hs.astronomymod.client;

import hs.astronomymod.item.AstronomyItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AstronomySlotComponent {
    // Server-side map for each player
    private static final Map<UUID, AstronomySlotComponent> PLAYER_COMPONENTS = new HashMap<>();
    // Client-side singleton (keep private!)
    private static final AstronomySlotComponent CLIENT_INSTANCE = new AstronomySlotComponent();

    private ItemStack astronomyStack = ItemStack.EMPTY;
    private int cooldown = 0;

    // --- Server-side accessor ---
    public static AstronomySlotComponent get(ServerPlayerEntity player) {
        return PLAYER_COMPONENTS.computeIfAbsent(player.getUuid(), k -> new AstronomySlotComponent());
    }

    // --- Client-side accessor ---
    public static AstronomySlotComponent getClient() {
        return CLIENT_INSTANCE;
    }

    // --- Server tick: passive ability ---
    public void tickServer(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbility(player);
        }
        if (cooldown > 0) cooldown--;
    }

    // --- Client tick: visuals/passive ---
    public void tickClient(ClientPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbilityClient(player);
        }
        if (cooldown > 0) cooldown--;
    }

    // --- Trigger active ability (via keybind) ---
    public void activateAbility(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem && cooldown <= 0) {
            astronomyItem.applyActiveAbility(player);
            cooldown = 600; // 30s cooldown
        }
    }

    // --- Getters/Setters: fully safe ---
    public ItemStack getAstronomyStack() {
        // Only return if valid
        if (!astronomyStack.isEmpty() && !(astronomyStack.getItem() instanceof AstronomyItem)) {
            astronomyStack = ItemStack.EMPTY;
        }
        return astronomyStack;
    }
    public void setAstronomyStack(ItemStack stack) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof AstronomyItem)) {
            this.astronomyStack = ItemStack.EMPTY;
        } else {
            this.astronomyStack = stack;
        }
    }

    public int getCooldown() { return cooldown; }
}