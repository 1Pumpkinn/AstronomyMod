package hs.astronomymod.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import hs.astronomymod.item.AstronomyItem;

public class AstronomyShard extends Item {
    public AstronomyShard(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack shardStack = player.getStackInHand(hand);
        ItemStack otherHandStack = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        // Check if other hand has an AstronomyItem
        if (otherHandStack.getItem() instanceof AstronomyItem) {
            int currentShards = otherHandStack.getOrDefault(
                    hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, 0
            );

            if (currentShards >= 3) {
                if (world instanceof net.minecraft.server.world.ServerWorld) {
                    player.sendMessage(Text.literal("§cThis item is already fully upgraded!"), true);
                }
                return ActionResult.FAIL;
            }

            if (world instanceof net.minecraft.server.world.ServerWorld) {
                // Upgrade the item
                otherHandStack.set(hs.astronomymod.component.ModComponents.ASTRONOMY_SHARDS, currentShards + 1);

                // Consume one shard
                shardStack.decrement(1);

                // Play sound and send message
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

                player.sendMessage(Text.literal("§6Upgraded to " + (currentShards + 1) + " shard(s)!"), true);

                return ActionResult.SUCCESS;
            }

            return ActionResult.SUCCESS;
        }

        if (world instanceof net.minecraft.server.world.ServerWorld) {
            player.sendMessage(Text.literal("§cHold an Astronomy Item in your other hand!"), true);
        }

        return ActionResult.FAIL;
    }
}