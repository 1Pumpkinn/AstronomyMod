package hs.astronomymod.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.component.type.TooltipDisplayComponent;

import java.util.function.Consumer;

public abstract class AstronomyItem extends Item {

    public AstronomyItem(Settings settings) {
        super(settings);
    }

    // --- Server-only logic ---
    public abstract void applyPassiveAbility(ServerPlayerEntity player);
    public abstract void applyActiveAbility(ServerPlayerEntity player);

    // --- Client-only logic (optional visuals) ---
    public void applyPassiveAbilityClient(ClientPlayerEntity player) {
        // Default: do nothing, can be overridden in subclasses
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        textConsumer.accept(Text.literal("§6Astronomy Item"));
        textConsumer.accept(Text.literal("§7Place in Astronomy Slot for abilities"));
        addCustomTooltip(textConsumer);
    }

    protected abstract void addCustomTooltip(Consumer<Text> textConsumer);
}
