package hs.astronomymod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AstronomyCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(AstronomyCommands::registerCommands);
    }

    private static void registerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(
                CommandManager.literal("astronomy")
                        .then(CommandManager.literal("equip")
                                .then(CommandManager.literal("planet")
                                        .executes(ctx -> equipItem(ctx, new ItemStack(ModItems.PLANET_ITEM))))
                                .then(CommandManager.literal("supernova")
                                        .executes(ctx -> equipItem(ctx, new ItemStack(ModItems.SUPERNOVA_ITEM))))
                                .then(CommandManager.literal("blackhole")
                                        .executes(ctx -> equipItem(ctx, new ItemStack(ModItems.BLACKHOLE_ITEM))))
                                .then(CommandManager.literal("neutron_star")
                                        .executes(ctx -> equipItem(ctx, new ItemStack(ModItems.NEUTRON_STAR_ITEM))))
                                .then(CommandManager.literal("pulsar")
                                        .executes(ctx -> equipItem(ctx, new ItemStack(ModItems.PULSAR_ITEM))))
                        )
                        .then(CommandManager.literal("unequip")
                                .executes(AstronomyCommands::unequipItem)
                        )
                        .then(CommandManager.literal("give")
                                .then(CommandManager.literal("all")
                                        .executes(AstronomyCommands::giveAllItems)
                                )
                        )
        );
    }

    private static int equipItem(CommandContext<ServerCommandSource> context, ItemStack stack) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            AstronomySlotComponent component = AstronomySlotComponent.get(player);

            // Return old item to inventory if present
            ItemStack oldStack = component.getAstronomyStack();
            if (!oldStack.isEmpty()) {
                player.giveItemStack(oldStack);
            }

            component.setAstronomyStack(stack.copy());
            player.sendMessage(Text.literal("§6Equipped: " + stack.getName().getString()), false);
            return 1;
        }
        return 0;
    }

    private static int unequipItem(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            AstronomySlotComponent component = AstronomySlotComponent.get(player);
            ItemStack stack = component.getAstronomyStack();

            if (!stack.isEmpty()) {
                player.giveItemStack(stack);
                component.setAstronomyStack(ItemStack.EMPTY);
                player.sendMessage(Text.literal("§6Unequipped astronomy item"), false);
            } else {
                player.sendMessage(Text.literal("§cNo item equipped"), false);
            }
            return 1;
        }
        return 0;
    }

    private static int giveAllItems(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            player.giveItemStack(new ItemStack(ModItems.PLANET_ITEM));
            player.giveItemStack(new ItemStack(ModItems.SUPERNOVA_ITEM));
            player.giveItemStack(new ItemStack(ModItems.BLACKHOLE_ITEM));
            player.giveItemStack(new ItemStack(ModItems.NEUTRON_STAR_ITEM));
            player.giveItemStack(new ItemStack(ModItems.PULSAR_ITEM));
            player.sendMessage(Text.literal("§6Given all astronomy items!"), false);
            return 1;
        }
        return 0;
    }
}