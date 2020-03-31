package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolsbase.api.control.parameters.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaitForItem {
    private final String ticket;
    private final ItemStack itemStack;
    private final Inventory inventory;

    public WaitForItem(@Nonnull String ticket, ItemStack itemStack, @Nullable Inventory inventory) {
        this.ticket = ticket;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }

    @Nonnull
    public String getTicket() {
        return ticket;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public Inventory getInventory() {
        return inventory;
    }
}
