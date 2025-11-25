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

    // Client-side singleton instance
    public static final AstronomySlotComponent CLIENT_INSTANCE = new AstronomySlotComponent();

    private ItemStack astronomyStack = ItemStack.EMPTY;
    private int cooldown = 0;

    // --- Server-side access ---
    public static AstronomySlotComponent get(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        return PLAYER_COMPONENTS.computeIfAbsent(uuid, k -> new AstronomySlotComponent());
    }

    // --- Client-side access ---
    public static AstronomySlotComponent getClient() {
        return CLIENT_INSTANCE;
    }

    // --- Server tick ---
    public void tickServer(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbility(player); // server logic
        }

        if (cooldown > 0) cooldown--;
    }

    // --- Client tick ---
    public void tickClient(ClientPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem) {
            astronomyItem.applyPassiveAbilityClient(player); // safe client call
        }
        if (cooldown > 0) cooldown--;
    }


    // --- Activate ability (server only) ---
    public void activateAbility(ServerPlayerEntity player) {
        if (!astronomyStack.isEmpty() && astronomyStack.getItem() instanceof AstronomyItem astronomyItem && cooldown <= 0) {
            astronomyItem.applyActiveAbility(player);
            cooldown = 600; // 30 second cooldown
        }
    }

    // --- Getters & setters ---
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
