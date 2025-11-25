package hs.astronomymod.network;

import hs.astronomymod.AstronomyMod;
import hs.astronomymod.client.AstronomySlotComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(ACTIVATE_ABILITY_ID, ActivateAbilityPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ACTIVATE_ABILITY_ID, (payload, player) -> {
            // Schedule on server thread
            player.getWorld().getServer().execute(() -> {
                AstronomySlotComponent component = AstronomySlotComponent.get(player);
                if (component != null) {
                    component.activateAbility(player);
                }
            });
        });
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(SYNC_SLOT_ID, SyncSlotPayload.CODEC);
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
}