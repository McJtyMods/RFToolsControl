package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolsbase.api.control.parameters.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record WaitForItem(@Nonnull String ticket, ItemStack itemStack,
                          @Nullable Inventory inventory) {
    public WaitForItem(@Nonnull String ticket, ItemStack itemStack, @Nullable Inventory inventory) {
        this.ticket = ticket;
        this.itemStack = itemStack;
        this.inventory = inventory;
    }
}
