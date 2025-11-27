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
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class AstronomyPackets {

    public static final CustomPayload.Id<ActivateAbilityPayload> ACTIVATE_ABILITY_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "activate_ability"));

    public static final CustomPayload.Id<SyncSlotPayload> SYNC_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "sync_slot"));

    public static final CustomPayload.Id<PlaceInSlotPayload> PLACE_IN_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "place_in_slot"));

    public static final CustomPayload.Id<TakeFromSlotPayload> TAKE_FROM_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "take_from_slot"));

    public static final CustomPayload.Id<SwapSlotPayload> SWAP_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "swap_slot"));

    public static final CustomPayload.Id<UpdateSlotPayload> UPDATE_SLOT_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "update_slot"));

    // Creative mode packet IDs
    public static final CustomPayload.Id<PlaceInSlotCreativePayload> PLACE_IN_SLOT_CREATIVE_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "place_in_slot_creative"));

    public static final CustomPayload.Id<TakeFromSlotCreativePayload> TAKE_FROM_SLOT_CREATIVE_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "take_from_slot_creative"));

    public static final CustomPayload.Id<SwapSlotCreativePayload> SWAP_SLOT_CREATIVE_ID =
            new CustomPayload.Id<>(Identifier.of(AstronomyMod.MOD_ID, "swap_slot_creative"));

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

        // Register PLACE in slot (survival mode)
        PayloadTypeRegistry.playC2S().register(PLACE_IN_SLOT_ID, PlaceInSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PLACE_IN_SLOT_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();

                // Validate slot is empty
                if (!currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Slot not empty, rejecting place");
                    return;
                }

                // Check cursor stack first (player is holding item)
                if (player.currentScreenHandler instanceof PlayerScreenHandler screenHandler) {
                    ItemStack cursorStack = screenHandler.getCursorStack();

                    if (!cursorStack.isEmpty() && cursorStack.getItem() instanceof AstronomyItem) {
                        // Take from cursor
                        ItemStack toPlace = cursorStack.copy();
                        toPlace.setCount(1);

                        cursorStack.decrement(1);
                        component.setAstronomyStack(toPlace);

                        // Sync to client
                        ServerPlayNetworking.send(player, new SyncSlotPayload(toPlace));
                        AstronomyMod.LOGGER.info("Placed {} in astronomy slot from cursor", toPlace.getName().getString());
                        return;
                    }
                }

                // If we get here, cursor didn't work - this shouldn't happen in normal gameplay
                AstronomyMod.LOGGER.warn("Place operation failed - no valid item on cursor");
            });
        });

        // Register TAKE from slot (survival mode)
        PayloadTypeRegistry.playC2S().register(TAKE_FROM_SLOT_ID, TakeFromSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TAKE_FROM_SLOT_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();
                if (currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Slot is empty, nothing to take");
                    return;
                }

                // Put on cursor if player has screen handler open
                if (player.currentScreenHandler instanceof PlayerScreenHandler screenHandler) {
                    ItemStack cursorStack = screenHandler.getCursorStack();

                    if (cursorStack.isEmpty()) {
                        // Put item on cursor
                        screenHandler.setCursorStack(currentSlot.copy());
                        component.setAstronomyStack(ItemStack.EMPTY);

                        // Sync to client
                        ServerPlayNetworking.send(player, new SyncSlotPayload(ItemStack.EMPTY));
                        AstronomyMod.LOGGER.info("Took {} from astronomy slot to cursor", currentSlot.getName().getString());
                        return;
                    }
                }

                // Fallback: give to inventory
                if (!player.giveItemStack(currentSlot.copy())) {
                    player.dropItem(currentSlot.copy(), false);
                }

                // Clear slot
                component.setAstronomyStack(ItemStack.EMPTY);

                // Sync to client
                ServerPlayNetworking.send(player, new SyncSlotPayload(ItemStack.EMPTY));
                AstronomyMod.LOGGER.info("Took {} from astronomy slot", currentSlot.getName().getString());
            });
        });

        // Register SWAP slot (survival mode)
        PayloadTypeRegistry.playC2S().register(SWAP_SLOT_ID, SwapSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SWAP_SLOT_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();

                if (currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Cannot swap with empty slot");
                    return;
                }

                // Check cursor stack
                if (player.currentScreenHandler instanceof PlayerScreenHandler screenHandler) {
                    ItemStack cursorStack = screenHandler.getCursorStack();

                    if (!cursorStack.isEmpty() && cursorStack.getItem() instanceof AstronomyItem) {
                        // Validate cursor has only 1 item for clean swap
                        if (cursorStack.getCount() != 1) {
                            AstronomyMod.LOGGER.warn("Cannot swap with stacked items");
                            return;
                        }

                        // Perform swap
                        ItemStack toPlace = cursorStack.copy();
                        ItemStack oldItem = currentSlot.copy();

                        // Update cursor and slot
                        screenHandler.setCursorStack(oldItem);
                        component.setAstronomyStack(toPlace);

                        // Sync to client
                        ServerPlayNetworking.send(player, new SyncSlotPayload(toPlace));
                        AstronomyMod.LOGGER.info("Swapped astronomy slot items");
                        return;
                    }
                }

                AstronomyMod.LOGGER.warn("Swap operation failed - no valid item on cursor");
            });
        });

        // Keep old UPDATE_SLOT for backwards compatibility / commands
        PayloadTypeRegistry.playC2S().register(UPDATE_SLOT_ID, UpdateSlotPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_SLOT_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack newStack = payload.stack();
                component.setAstronomyStack(newStack);
                ServerPlayNetworking.send(player, new SyncSlotPayload(newStack));
            });
        });

        // ===== CREATIVE MODE PACKETS =====

        // Register PLACE in slot (creative mode)
        PayloadTypeRegistry.playC2S().register(PLACE_IN_SLOT_CREATIVE_ID, PlaceInSlotCreativePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PLACE_IN_SLOT_CREATIVE_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();

                // Validate slot is empty
                if (!currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Creative: Slot not empty, rejecting place");
                    return;
                }

                // In creative mode, we just directly place the item
                ItemStack toPlace = payload.stack();
                if (toPlace.isEmpty() || !(toPlace.getItem() instanceof AstronomyItem)) {
                    AstronomyMod.LOGGER.warn("Creative: Invalid item for astronomy slot");
                    return;
                }

                // Place single item in slot - don't give anything back to player
                ItemStack singleItem = toPlace.copy();
                singleItem.setCount(1);
                component.setAstronomyStack(singleItem);
                ServerPlayNetworking.send(player, new SyncSlotPayload(singleItem));
                AstronomyMod.LOGGER.info("Creative: Placed {} in astronomy slot", toPlace.getName().getString());
            });
        });

        // Register TAKE from slot (creative mode)
        PayloadTypeRegistry.playC2S().register(TAKE_FROM_SLOT_CREATIVE_ID, TakeFromSlotCreativePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TAKE_FROM_SLOT_CREATIVE_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();
                if (currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Creative: Slot is empty, nothing to take");
                    return;
                }

                // In creative mode, just clear the slot - don't give item back
                // (Creative mode players can get items from the creative menu)
                component.setAstronomyStack(ItemStack.EMPTY);
                ServerPlayNetworking.send(player, new SyncSlotPayload(ItemStack.EMPTY));
                AstronomyMod.LOGGER.info("Creative: Removed {} from astronomy slot", currentSlot.getName().getString());
            });
        });

        // Register SWAP slot (creative mode)
        PayloadTypeRegistry.playC2S().register(SWAP_SLOT_CREATIVE_ID, SwapSlotCreativePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SWAP_SLOT_CREATIVE_ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component == null) return;

                ItemStack currentSlot = component.getAstronomyStack();
                if (currentSlot.isEmpty()) {
                    AstronomyMod.LOGGER.warn("Creative: Cannot swap with empty slot");
                    return;
                }

                ItemStack toPlace = payload.stack();
                if (toPlace.isEmpty() || !(toPlace.getItem() instanceof AstronomyItem)) {
                    AstronomyMod.LOGGER.warn("Creative: Invalid item for swap");
                    return;
                }

                // In creative mode: just replace the item, don't give old one back
                // (Creative players can get items from creative menu)
                ItemStack singleItem = toPlace.copy();
                singleItem.setCount(1);
                component.setAstronomyStack(singleItem);
                ServerPlayNetworking.send(player, new SyncSlotPayload(singleItem));
                AstronomyMod.LOGGER.info("Creative: Swapped astronomy slot items");
            });
        });
    }

    // Server-side registration for S2C packets (encoding)
    public static void registerS2CPacketsServer() {
        PayloadTypeRegistry.playS2C().register(SYNC_SLOT_ID, SyncSlotPayload.CODEC);
    }

    // Client-side registration for S2C packets (decoding + handling)
    public static void registerS2CPacketsClient() {
        // Register client handler for sync packets
        // Note: Payload type is already registered on server side
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

    // ===== PAYLOAD RECORDS =====

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

    public record PlaceInSlotPayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, PlaceInSlotPayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.OPTIONAL_PACKET_CODEC, PlaceInSlotPayload::stack,
                        PlaceInSlotPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return PLACE_IN_SLOT_ID;
        }
    }

    public record TakeFromSlotPayload() implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, TakeFromSlotPayload> CODEC =
                PacketCodec.unit(new TakeFromSlotPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return TAKE_FROM_SLOT_ID;
        }
    }

    public record SwapSlotPayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SwapSlotPayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.OPTIONAL_PACKET_CODEC, SwapSlotPayload::stack,
                        SwapSlotPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return SWAP_SLOT_ID;
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

    // ===== CREATIVE MODE PAYLOADS =====

    public record PlaceInSlotCreativePayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, PlaceInSlotCreativePayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.OPTIONAL_PACKET_CODEC, PlaceInSlotCreativePayload::stack,
                        PlaceInSlotCreativePayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return PLACE_IN_SLOT_CREATIVE_ID;
        }
    }

    public record TakeFromSlotCreativePayload() implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, TakeFromSlotCreativePayload> CODEC =
                PacketCodec.unit(new TakeFromSlotCreativePayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return TAKE_FROM_SLOT_CREATIVE_ID;
        }
    }

    public record SwapSlotCreativePayload(ItemStack stack) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SwapSlotCreativePayload> CODEC =
                PacketCodec.tuple(
                        ItemStack.OPTIONAL_PACKET_CODEC, SwapSlotCreativePayload::stack,
                        SwapSlotCreativePayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return SWAP_SLOT_CREATIVE_ID;
        }
    }
}