package hs.astronomymod.client.screen;

import hs.astronomymod.client.AstronomySlotComponent;
import hs.astronomymod.item.AstronomyItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class AstronomySlotScreenHandler extends ScreenHandler {
    private final Inventory astronomyInventory;
    private final PlayerEntity player;

    public AstronomySlotScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X1, syncId);
        this.player = playerInventory.player;
        this.astronomyInventory = new SimpleInventory(1);

        // Load current astronomy item
        if (player instanceof ServerPlayerEntity serverPlayer) {
            AstronomySlotComponent component = AstronomySlotComponent.get(serverPlayer);
            astronomyInventory.setStack(0, component.getAstronomyStack());
        }

        // Add astronomy slot (positioned to the right of player model in inventory)
        this.addSlot(new Slot(astronomyInventory, 0, 77, 44) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof AstronomyItem;
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        // Add player inventory slots (standard positions)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slot);

        if (clickedSlot.hasStack()) {
            ItemStack originalStack = clickedSlot.getStack();
            newStack = originalStack.copy();

            if (slot == 0) {
                // Moving from astronomy slot to inventory
                if (!this.insertItem(originalStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from inventory to astronomy slot
                if (originalStack.getItem() instanceof AstronomyItem) {
                    if (!this.insertItem(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        // Save astronomy item when closing inventory
        if (player instanceof ServerPlayerEntity serverPlayer) {
            AstronomySlotComponent component = AstronomySlotComponent.get(serverPlayer);
            component.setAstronomyStack(astronomyInventory.getStack(0));
        }
    }
}