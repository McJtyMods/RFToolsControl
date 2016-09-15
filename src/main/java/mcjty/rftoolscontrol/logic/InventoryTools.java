package mcjty.rftoolscontrol.logic;

import mcjty.rftools.api.storage.IStorageScanner;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

public class InventoryTools {

    public static int countItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, ItemStack itemMatcher, boolean oredict, int maxToCount) {
        if (itemHandler != null) {
            // @todo implement oredict here
            int cnt = 0;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                    cnt += stack.stackSize;
                    if (maxToCount != -1 && cnt >= maxToCount) {
                        return maxToCount;
                    }
                }
            }
            return cnt;
        } else if (scanner != null) {
            int cnt = scanner.countItems(itemMatcher, true, oredict);
            if (maxToCount != -1 && cnt >= maxToCount) {
                return maxToCount;
            }
            return cnt;
        }
        return 0;
    }

    @Nullable
    public static ItemStack extractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, int amount, boolean routable, boolean oredict, @Nullable ItemStack itemMatcher,
                                        @Nullable Integer slot) {
        if (itemHandler != null) {
            // @todo implement oredict here
            if (slot == null) {
                if (itemMatcher == null) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null) {
                            return itemHandler.extractItem(i, amount, false);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                            return itemHandler.extractItem(i, amount, false);
                        }
                    }
                }
            } else {
                return itemHandler.extractItem(slot, amount, false);
            }
        } else if (scanner != null) {
            return scanner.requestItem(itemMatcher, amount, routable, oredict);
        }
        return null;
    }

    @Nullable
    public static ItemStack tryExtractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, int amount, boolean routable, boolean oredict, @Nullable ItemStack itemMatcher,
                                        @Nullable Integer slot) {

        if (itemHandler != null) {
            if (slot == null) {
                if (itemMatcher == null) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null) {
                            return itemHandler.extractItem(i, amount, true);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                            return itemHandler.extractItem(i, amount, true);
                        }
                    }
                }
            } else {
                return itemHandler.extractItem(slot, amount, true);
            }
        }
        if (scanner != null) {
            int cnt = scanner.countItems(itemMatcher, routable, oredict);
            if (cnt > 0) {
                ItemStack copy = itemMatcher.copy();
                copy.stackSize = Math.min(cnt, amount);
                return copy;
            }
        }
        return null;
    }

    @Nullable
    public static ItemStack insertItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, @Nonnull ItemStack item,
                                        @Nullable Integer slot) {
        if (itemHandler != null) {
            if (slot == null) {
                return ItemHandlerHelper.insertItem(itemHandler, item, false);
            } else {
                return itemHandler.insertItem(slot, item, false);
            }
        }
        if (scanner != null) {
            int cnt = scanner.insertItem(item);
            if (cnt > 0) {
                ItemStack copy = item.copy();
                copy.stackSize = cnt;
                return copy;
            }
            return null;
        }
        return item;
    }
}
