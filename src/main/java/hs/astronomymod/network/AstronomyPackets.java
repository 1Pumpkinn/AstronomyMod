package hs.astronomymod.network;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public class AstronomyPackets {

    public static final CustomPayload.Id<ActivateAbilityPayload> ACTIVATE_ABILITY_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "activate_ability"));

    public static final CustomPayload.Id<SyncSlotPayload> SYNC_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "sync_slot"));

    public static final CustomPayload.Id<UpdateSlotPayload> UPDATE_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "update_slot"));

    public static void registerC2SPackets() {
        // Register activate ability packet
        PayloadTypeRegistry.playC2S().register(ACTIVATE_ABILITY_ID, ActivateAbilityPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ACTIVATE_ABILITY_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component != null) {
                    component.activateAbility(player);
                }
            });
        });

        // Register update slot packet (C2S)
        PayloadTypeRegistry.playC2S().register(UPDATE_SLOT_ID, UpdateSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_SLOT_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component != null) {
                    ItemStack stack = payload.stack();

                    // Validate that the item is an AstronomyItem
                    if (!stack.isEmpty() && !(stack.getItem() instanceof AstronomyItem)) {
                        AstronomyMod.LOGGER.warn("Player " + player.getName().getString() +
                                " attempted to place non-astronomy item in slot: " + stack.getName().getString());
                        // Send back the current valid stack
                        ServerPlayNetworking.send(player, new SyncSlotPayload(0));
                        return;
                    }

                    component.setAstronomyStack(stack);
                    AstronomyMod.LOGGER.info("Updated astronomy slot for player: " +
                            player.getName().getString() + " with item: " +
                            (stack.isEmpty() ? "EMPTY" : stack.getName().getString()));
                    // Always sync back to client to confirm stack
                    ServerPlayNetworking.send(player, new SyncSlotPayload(0));
                }
            });
        });
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(SYNC_SLOT_ID, SyncSlotPayload.CODEC);

        // Register client handler for sync packets
        ClientPlayNetworking.registerGlobalReceiver(SYNC_SLOT_ID, (payload, context) -> {
            context.client().execute(() -> {
                // This is just a signal to refresh from server
                // In a full implementation, you'd send the actual ItemStack here
                AstronomyMod.LOGGER.info("Received sync request from server");
            });
        });
    }

    public record ActivateAbilityPayload() implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, ActivateAbilityPayload> CODEC =
                PacketCodec.unit(new ActivateAbilityPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ACTIVATE_ABILITY_ID;
        }
    }

    public record SyncSlotPayload(int slot) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SyncSlotPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, SyncSlotPayload::slot,
                        SyncSlotPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return SYNC_SLOT_ID;
        }
    }

    public record UpdateSlotPayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, UpdateSlotPayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.PACKET_CODEC, UpdateSlotPayload::stack,
                        UpdateSlotPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return UPDATE_SLOT_ID;
        }
    }
}