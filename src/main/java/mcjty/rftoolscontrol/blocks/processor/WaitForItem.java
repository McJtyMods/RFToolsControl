package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.registry.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class WaitForItem {
    private final String ticket;
    private final ItemStack itemStack;
    private final Inventory inventory;

    public WaitForItem(@Nonnull String ticket, @Nonnull ItemStack itemStack, @Nonnull Inventory inventory) {
        this.ticket = ticket;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }

    @Nonnull
    public String getTicket() {
        return ticket;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nonnull
    public Inventory getInventory() {
        return inventory;
    }
}
