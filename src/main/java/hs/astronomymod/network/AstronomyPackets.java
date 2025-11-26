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
                    AstronomyMod.LOGGER.info("Player {} activated astronomy ability", player.getName().getString());
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

                    // Validate that the item is an AstronomyItem or empty
                    if (!stack.isEmpty() && !(stack.getItem() instanceof AstronomyItem)) {
                        AstronomyMod.LOGGER.warn("Player {} attempted to place non-astronomy item in slot: {}",
                                player.getName().getString(), stack.getName().getString());
                        // Reject and sync back current state
                        ItemStack currentStack = component.getAstronomyStack();
                        ServerPlayNetworking.send(player, new SyncSlotPayload(currentStack));
                        return;
                    }

                    // Update server-side component
                    component.setAstronomyStack(stack);

                    AstronomyMod.LOGGER.info("Updated astronomy slot for player: {} with item: {}",
                            player.getName().getString(),
                            stack.isEmpty() ? "EMPTY" : stack.getName().getString());

                    // Sync back to client to confirm
                    ServerPlayNetworking.send(player, new SyncSlotPayload(stack));
                }
            });
        });
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(SYNC_SLOT_ID, SyncSlotPayload.CODEC);

        // Register client handler for sync packets
        ClientPlayNetworking.registerGlobalReceiver(SYNC_SLOT_ID, (payload, context) -> {
            context.client().execute(() -> {
                AstronomySlotComponent clientComponent = AstronomySlotComponent.getClient();
                ItemStack syncedStack = payload.stack();

                // Update client-side component with server's authoritative state
                clientComponent.setAstronomyStack(syncedStack);

                AstronomyMod.LOGGER.info("Client received sync: {}",
                        syncedStack.isEmpty() ? "EMPTY" : syncedStack.getName().getString());
            });
        });
    }

    // Payload Records
    public record ActivateAbilityPayload() implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, ActivateAbilityPayload> CODEC =
                PacketCodec.unit(new ActivateAbilityPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ACTIVATE_ABILITY_ID;
        }
    }

    public record SyncSlotPayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SyncSlotPayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.OPTIONAL_PACKET_CODEC, SyncSlotPayload::stack,
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
                        ItemStack.OPTIONAL_PACKET_CODEC, UpdateSlotPayload::stack,
                        UpdateSlotPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return UPDATE_SLOT_ID;
        }
    }
}