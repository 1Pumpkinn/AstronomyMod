package hs.astronomymod;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.command.AstronomyCommands;
import hs.astronomymod.component.ModComponents;
import hs.astronomymod.effect.ModStatusEffects;
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
        ModStatusEffects.registerStatusEffects();
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
            
            // Handle stun status effect - prevent movement for all entities with stun
            var stunEntry = hs.astronomymod.effect.ModStatusEffects.getStunEntry();
            if (stunEntry != null) {
                // Iterate through all players on the server
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (player.getStatusEffects().stream().anyMatch(effect -> 
                        effect.getEffectType().equals(stunEntry))) {
                        // Prevent horizontal movement but allow vertical (falling)
                        player.setVelocity(0, player.getVelocity().y, 0);
                        player.velocityModified = true;
                    }
                }
                
                // Also handle non-player entities in all worlds
                server.getWorlds().forEach(world -> {
                    if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                        // Use a reasonable bounding box - check entities near players
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            if (player.getEntityWorld() == serverWorld) {
                                java.util.List<net.minecraft.entity.LivingEntity> entities = 
                                    serverWorld.getEntitiesByClass(
                                        net.minecraft.entity.LivingEntity.class,
                                        player.getBoundingBox().expand(50),
                                        entity -> entity != player && entity.getStatusEffects().stream().anyMatch(effect -> 
                                            effect.getEffectType().equals(stunEntry))
                                    );
                                
                                entities.forEach(entity -> {
                                    // Prevent horizontal movement but allow vertical (falling)
                                    entity.setVelocity(0, entity.getVelocity().y, 0);
                                    entity.velocityModified = true;
                                });
                            }
                        }
                    }
                });
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

        // Register attack event for Blackhole Ability 1 (Stun on next hit)
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, newHealth, blocked) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
                var astronomyStack = AstronomySlotComponent.get(attacker).getAstronomyStack();
                if (!astronomyStack.isEmpty() && 
                    astronomyStack.getItem() instanceof hs.astronomymod.item.custom.BlackholeItem) {
                    // Check if stun is ready and apply it
                    hs.astronomymod.abilities.blackhole.active.GravitationalPullActive.applyStun(attacker, entity);
                }
            }
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