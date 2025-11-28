package hs.astronomymod.client;

import hs.astronomymod.abilities.AbilityActivation;
import hs.astronomymod.item.AstronomyItem;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AstronomySlotComponent {
    // Server-side map for each player
    private static final Map<UUID, AstronomySlotComponent> PLAYER_COMPONENTS = new HashMap<>();
    // Client-side singleton
    private static final AstronomySlotComponent CLIENT_INSTANCE = new AstronomySlotComponent();

    private static final float MAX_OVERLOAD = 6.0f;
    private static final int OVERLOAD_RECOVERY_TICKS = 35 * 20;
    private static final float OVERLOAD_DECAY_PER_TICK = MAX_OVERLOAD / OVERLOAD_RECOVERY_TICKS;
    private static final float SYNC_THRESHOLD = 0.02f;

    private ItemStack astronomyStack = ItemStack.EMPTY;
    private float overloadLevel = 0f;
    private float lastSyncedOverload = -1f;

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
        if (decayOverloadInternal()) {
            syncOverload(player);
        }
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
        decayOverloadInternal();
    }

    // --- Trigger active ability (via keybind) ---
    public void activateAbility(ServerPlayerEntity player, AbilityActivation activation) {
        if (isOverloaded()) {
            player.sendMessage(Text.translatable("message.astronomymod.overloaded"), true);
            return;
        }

        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            if (astronomyItem.applyActiveAbility(player, activation)) {
                increaseOverload(player);
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

    public float getOverloadLevel() {
        return overloadLevel;
    }

    public void setOverloadLevel(float overloadLevel) {
        this.overloadLevel = Math.max(0f, Math.min(MAX_OVERLOAD, overloadLevel));
    }

    public static float getMaxOverload() {
        return MAX_OVERLOAD;
    }

    private void increaseOverload(ServerPlayerEntity player) {
        overloadLevel = Math.min(MAX_OVERLOAD, overloadLevel + 1f);
        syncOverload(player);
    }

    private boolean decayOverloadInternal() {
        if (overloadLevel <= 0f) return false;
        float newLevel = Math.max(0f, overloadLevel - OVERLOAD_DECAY_PER_TICK);
        if (Math.abs(newLevel - overloadLevel) > 0.0001f) {
            overloadLevel = newLevel;
            return true;
        }
        return false;
    }

    private boolean isOverloaded() {
        return overloadLevel >= MAX_OVERLOAD - 0.0001f;
    }

    private void syncOverload(ServerPlayerEntity player) {
        if (Math.abs(overloadLevel - lastSyncedOverload) < SYNC_THRESHOLD) return;
        lastSyncedOverload = overloadLevel;
        ServerPlayNetworking.send(player, new AstronomyPackets.SyncOverloadPayload(overloadLevel));
    }
}