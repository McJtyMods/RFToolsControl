package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.registry.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaitForItem {
    private final String ticket;
    private final ItemStack itemStack;
    private final Inventory inventory;

    public WaitForItem(@Nonnull String ticket, @Nullable ItemStack itemStack, @Nullable Inventory inventory) {
        this.ticket = ticket;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }

    @Nonnull
    public String getTicket() {
        return ticket;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public Inventory getInventory() {
        return inventory;
    }
}
