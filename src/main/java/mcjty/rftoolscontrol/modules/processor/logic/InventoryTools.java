package mcjty.rftoolscontrol.modules.processor.logic;

import mcjty.rftoolsbase.api.control.parameters.BlockSide;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class InventoryTools {

    public static int countItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, Ingredient itemMatcher, int maxToCount) {
        if (itemHandler != null) {
            int cnt = 0;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (itemMatcher.test(stack)) {
                    cnt += stack.getCount();
                    if (maxToCount != -1 && cnt >= maxToCount) {
                        return maxToCount;
                    }
                }
            }
            return cnt;
        } else if (scanner != null) {
            int cnt = scanner.countItems(itemMatcher, true, null);
            if (maxToCount != -1 && cnt >= maxToCount) {
                return maxToCount;
            }
            return cnt;
        }
        return 0;
    }

    public static boolean areItemsEqual(ItemStack item1, ItemStack item2, boolean damage, boolean nbt) {
        if (item1.getItem() != item2.getItem()) {
            return false;
        }
        if (damage && item1.getDamage() != item2.getDamage()) {
            return false;
        }
        if (nbt && !ItemStack.areItemStackTagsEqual(item1, item2)) {
            return false;
        }
        return true;
    }

    private static boolean isItemEqual(ItemStack thisItem, ItemStack other) {
        if (other.isEmpty()) {
            return false;
        }
        return thisItem.isItemEqual(other);
    }


    public static ItemStack extractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                        @Nullable Integer amount, boolean routable, @Nonnull Ingredient itemMatcher,
                                        @Nullable Integer slot) {
        if (itemHandler != null) {
            // @todo implement oredict here
            if (slot == null) {
                if (itemMatcher == Ingredient.EMPTY) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (!stack.isEmpty() && (amount == null || amount <= stack.getCount())) {
                            return itemHandler.extractItem(i, amount == null ? 64 : amount, false);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (itemMatcher.test(stack)) {
                            return itemHandler.extractItem(i, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, false);
                        }
                    }
                }
            } else {
                if (itemMatcher == Ingredient.EMPTY) {
                    return itemHandler.extractItem(slot, amount == null ? 64 : amount, false);
                } else {
                    if (!itemMatcher.test(itemHandler.getStackInSlot(slot))) {
                        return ItemStack.EMPTY;
                    }
                    return itemHandler.extractItem(slot, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, false);
                }
            }
        } else if (scanner != null) {
            return scanner.requestItem(itemMatcher, false, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, routable); // @todo 1.15 check
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack extractItem(@Nullable IItemHandler itemHandler,
                                        int amount, @Nonnull Predicate<ItemStack> itemMatcher) {
        if (itemHandler != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (itemMatcher.test(stack)) {
                    return itemHandler.extractItem(i, amount, false);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack tryExtractItem(@Nullable IItemHandler itemHandler,
                                           int amount,
                                           Predicate<ItemStack> itemMatcher) {

        if (itemHandler != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && itemMatcher.test(stack)) {
                    return itemHandler.extractItem(i, amount, true);
                }
            }
        }
        return ItemStack.EMPTY;
    }



    public static ItemStack tryExtractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                           @Nullable Integer amount, boolean routable,
                                           Ingredient itemMatcher,
                                           @Nullable Integer slot) {

        if (itemHandler != null) {
            if (slot == null) {
                if (itemMatcher == Ingredient.EMPTY) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (!stack.isEmpty() && (amount == null || amount <= stack.getCount())) {
                            return itemHandler.extractItem(i, amount == null ? 64 : amount, true);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (!stack.isEmpty() && itemMatcher.test(stack)) {
                            return itemHandler.extractItem(i, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, true);
                        }
                    }
                }
            } else {
                if (itemMatcher == Ingredient.EMPTY) {
                    return itemHandler.extractItem(slot, amount == null ? 64 : amount, true);
                } else {
                    if (!itemMatcher.test(itemHandler.getStackInSlot(slot))) {
                        return ItemStack.EMPTY;
                    }
                    return itemHandler.extractItem(slot, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, true);
                }
            }
        }
        if (scanner != null) {
            return scanner.getItem(itemMatcher, routable);
        }
        return ItemStack.EMPTY;
    }

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
                copy.setCount(cnt);
                return copy;
            }
            return ItemStack.EMPTY;
        }
        return item;
    }

    public static String inventoryToString(Inventory inv) {
        String s = StringUtils.left(inv.getSide().getName().toUpperCase(), 1);
        if (inv.getIntSide() == null) {
            s += "/*";
        } else {
            String is = StringUtils.left(inv.getIntSide().getName().toUpperCase(), 1);
            s += "/" + is;
        }
        if (inv.getNodeName() == null) {
            return s;
        } else {
            return inv.getNodeName() + " " + s;
        }
    }

    @Nullable
    public static Inventory inventoryFromString(String s) {
        if (s == null) {
            return null;
        }
        int indexOf = s.lastIndexOf('/');
        if (indexOf == -1) {
            return null;
        }
        if (s.length() <= indexOf+1) {
            return null;
        }
        Direction side = getSideFromChar(s.charAt(indexOf-1));
        if (side == null) {
            // Side == null is invalid for Inventory
            return null;
        }
        Direction intSide = getSideFromChar(s.charAt(indexOf+1));

        int indexSpace = s.lastIndexOf(' ');
        if (indexSpace <= 0) {
            return new Inventory(null, side, intSide);
        }

        return new Inventory(s.substring(0, indexSpace), side, intSide);
    }

    public static Direction getSideFromChar(char is) {
        switch (is) {
            case '*': return null;
            case 'D': return Direction.DOWN;
            case 'U': return Direction.UP;
            case 'W': return Direction.WEST;
            case 'E': return Direction.EAST;
            case 'S': return Direction.SOUTH;
            case 'N': return Direction.NORTH;
        }
        return null;
    }

    public static String blockSideToString(BlockSide bc) {
        String s;
        if (bc.getSide() == null) {
            s = "*";
        } else {
            s = StringUtils.left(bc.getSide().getName().toUpperCase(), 1);
        }
        if (bc.getNodeName() == null) {
            return s;
        } else {
            return bc.getNodeName() + " " + s;
        }
    }

    @Nullable
    public static BlockSide blockSideFromString(String s) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty()) {
            return null;
        }
        Direction side = getSideFromChar(s.charAt(s.length()-1));
        int indexOf = s.lastIndexOf(' ');
        if (indexOf <= 0) {
            return new BlockSide(null, side);
        }
        return new BlockSide(s.substring(0, indexOf), side);
    }

    // @todo 1.15 is this the right way?
    public static int getCountFromIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length > 0) {
            return stacks[0].getCount();
        } else {
            return 1;   // Unknown
        }
    }

    // @todo 1.15 is this the right way?
    public static int getMaxStackSizeFromIngredient(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        if (stacks.length > 0) {
            return stacks[0].getMaxStackSize();
        } else {
            return 1;   // Unknown
        }
    }
}
