package hs.astronomymod;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.command.AstronomyCommands;
import hs.astronomymod.component.ModComponents;
import hs.astronomymod.item.ModItems;
import hs.astronomymod.network.AstronomyPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstronomyMod implements ModInitializer {
    public static final String MOD_ID = "astronomymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Astronomy Mod Initializing...");

        ModComponents.registerComponents();
        ModItems.registerModItems();
        AstronomyCommands.register();

        // Register C2S packets
        AstronomyPackets.registerC2SPackets();

        // Register S2C packets on server side
        AstronomyPackets.registerS2CPacketsServer();

        // Tick event for abilities
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                AstronomySlotComponent comp = AstronomySlotComponent.get(player);
                if (comp != null) {
                    comp.tickServer(player);

                    // Special handling for Neutron Star beam
                    var stack = comp.getAstronomyStack();
                    if (!stack.isEmpty() && stack.getItem() instanceof hs.astronomymod.item.custom.NeutronStarItem) {
                        // Tick the plasma beam (handled in NeutronStarAbility.applyPassive via PlasmaBeamActive.tickBeam)
                        // This is already handled in the ability's applyPassive method
                    }
                }
            }
        });

        // Sync astronomy slot when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            AstronomySlotComponent component = AstronomySlotComponent.get(player);

            // Send current slot state to client
            ServerPlayNetworking.send(player, new AstronomyPackets.SyncSlotPayload(component.getAstronomyStack()));
            LOGGER.info("Synced astronomy slot for player: {}", player.getName().getString());
        });

        // Clean up player data on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            AstronomySlotComponent.remove(handler.getPlayer().getUuid());
            LOGGER.info("Cleaned up astronomy data for player: {}", handler.getPlayer().getName().getString());
        });

        // Register damage event for mace/density damage protection (Blackhole Passive 2)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                var astronomyStack = AstronomySlotComponent.get(player).getAstronomyStack();
                int shards = astronomyStack.getOrDefault(ModComponents.ASTRONOMY_SHARDS, 0);
                
                // Check if player has Blackhole item with 2+ shards (DensityShieldPassive)
                if (shards >= 2 && !astronomyStack.isEmpty() && 
                    astronomyStack.getItem() instanceof hs.astronomymod.item.custom.BlackholeItem) {
                    
                    // Check if damage is from mace/density damage
                    // In Minecraft 1.21+, maces deal density damage
                    // Check if the attacker is using a mace or if it's density damage
                    if (source.getAttacker() != null && source.getAttacker() instanceof net.minecraft.entity.LivingEntity attacker) {
                        var mainHand = attacker.getMainHandStack();
                        if (mainHand.getItem() instanceof net.minecraft.item.MaceItem) {
                            return false; // Cancel mace damage
                        }
                    }
                    // Also check for density damage type if available
                    try {
                        if (source.isOf(DamageTypes.PLAYER_ATTACK) && source.getName().contains("density")) {
                            return false;
                        }
                    } catch (Exception e) {
                        // Damage type might not exist, continue
                    }
                }
            }
            return true; // Allow damage
        });

        LOGGER.info("Astronomy Mod Initialized!");
    }
}