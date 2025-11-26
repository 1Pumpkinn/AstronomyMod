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
    // Client-side singleton
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

    // --- Remove player data on disconnect ---
    public static void remove(UUID playerUuid) {
        PLAYER_COMPONENTS.remove(playerUuid);
    }

    // --- Server tick: passive ability ---
    public void tickServer(ServerPlayerEntity player) {
        // Validate stack is still an AstronomyItem
        if (!astronomyStack.isEmpty() && !(astronomyStack.getItem() instanceof AstronomyItem)) {
            astronomyStack = ItemStack.EMPTY;
        }

        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbility(player);
        }
        if (cooldown > 0) cooldown--;
    }

    // --- Client tick: visuals/passive ---
    public void tickClient(ClientPlayerEntity player) {
        // Validate stack is still an AstronomyItem
        if (!astronomyStack.isEmpty() && !(astronomyStack.getItem() instanceof AstronomyItem)) {
            astronomyStack = ItemStack.EMPTY;
        }

        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbilityClient(player);
        }
        if (cooldown > 0) cooldown--;
    }

    // --- Trigger active ability (via keybind) ---
    public void activateAbility(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem && cooldown <= 0) {
            int shards = astronomyStack.getOrDefault(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0);

            if (shards >= 3) {
                astronomyItem.applyActiveAbility(player);
                cooldown = 600; // 30s cooldown (600 ticks = 30 seconds)
            } else {
                player.sendMessage(net.minecraft.text.Text.literal("§cNeed 3 shards to use active ability!"), true);
            }
        }
    }

    // --- Getters/Setters ---
    public ItemStack getAstronomyStack() {
        // Validate before returning
        if (!astronomyStack.isEmpty() && !(astronomyStack.getItem() instanceof AstronomyItem)) {
            astronomyStack = ItemStack.EMPTY;
        }
        return astronomyStack;
    }

    public void setAstronomyStack(ItemStack stack) {
        // Only accept empty or valid AstronomyItem stacks
        if (stack.isEmpty()) {
            this.astronomyStack = ItemStack.EMPTY;
        } else if (stack.getItem() instanceof AstronomyItem) {
            this.astronomyStack = stack.copy();
        } else {
            this.astronomyStack = ItemStack.EMPTY;
        }
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}