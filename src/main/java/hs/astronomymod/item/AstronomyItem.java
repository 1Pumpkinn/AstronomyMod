package hs.astronomymod.item;

import hs.astronomymod.abilities.Ability;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.component.type.TooltipDisplayComponent;

import java.util.function.Consumer;

public abstract class AstronomyItem extends Item {

    private final Ability ability;

    public AstronomyItem(Settings settings, Ability ability) {
        super(settings);
        this.ability = ability;
    }

    // --- Server-only logic ---
    public void applyPassiveAbility(ServerPlayerEntity player) {
        if (ability != null) {
            ability.applyPassive(player);
        }
    }

    public void applyActiveAbility(ServerPlayerEntity player) {
        if (ability != null) {
            ability.applyActive(player);
        }
    }

    // --- Client-only logic (optional visuals) ---
    public void applyPassiveAbilityClient(ClientPlayerEntity player) {
        if (ability != null) {
            ability.applyPassiveClient(player);
        }
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
        addCustomTooltip(textConsumer, stack);
    }

    protected abstract void addCustomTooltip(Consumer<Text> textConsumer, ItemStack stack);
}