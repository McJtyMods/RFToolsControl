package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.registry.Inventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class WaitForItem {
    private final String craftId;
    private final ItemStack itemStack;
    private final Inventory inventory;

    public WaitForItem(@Nonnull String craftId, @Nonnull ItemStack itemStack, @Nonnull Inventory inventory) {
        this.craftId = craftId;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }

    @Nonnull
    public String getCraftId() {
        return craftId;
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
