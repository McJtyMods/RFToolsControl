package mcjty.rftoolscontrol.logic;

import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolscontrol.api.parameters.BlockSide;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class InventoryTools {

    public static int countItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner, Ingredient itemMatcher, boolean oredict, int maxToCount) {
        if (itemHandler != null) {
            // @todo 1.15 get rid of oredict stuff. Not working
            Set<Integer> oredictMatchers = getOredictMatchers(itemMatcher, oredict);
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

    public static boolean areItemsEqual(ItemStack item1, ItemStack item2, boolean meta, boolean nbt, boolean oredict) {
        if (oredict) {
            // @todo 1.15 ore dict
//            if (!OreDictionary.itemMatches(item1, item2, false)) {
//                return false;
//            }
        } else {
            if (item1.getItem() != item2.getItem()) {
                return false;
            }
        }
        if (meta && item1.getDamage() != item2.getDamage()) {
            return false;
        }
        if (nbt && !ItemStack.areItemStackTagsEqual(item1, item2)) {
            return false;
        }
        return true;
    }

    private static Set<Integer> getOredictMatchers(Ingredient stack, boolean oredict) {
        Set<Integer> oredictMatches = new HashSet<>();
        if (oredict) {
// @todo 1.15
            //            for (int id : OreDictionary.getOreIDs(stack)) {
//                oredictMatches.add(id);
//            }
        }
        return oredictMatches;
    }

    private static boolean isItemEqual(ItemStack thisItem, ItemStack other, Set<Integer> oreDictMatchers) {
        if (other.isEmpty()) {
            return false;
        }
        if (oreDictMatchers.isEmpty()) {
            return thisItem.isItemEqual(other);
        } else {
// @todo 1.15 ore dict
            //            int[] oreIDs = OreDictionary.getOreIDs(other);
//            for (int id : oreIDs) {
//                if (oreDictMatchers.contains(id)) {
//                    return true;
//                }
//            }
        }
        return false;
    }


    public static ItemStack extractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                        @Nullable Integer amount, boolean routable, boolean oredict, boolean strictnbt, @Nonnull Ingredient itemMatcher,
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
                        if (isEqualAdvanced(itemMatcher, stack, strictnbt)) {
                            return itemHandler.extractItem(i, amount == null ? getMaxStackSizeFromIngredient(itemMatcher) : amount, false);
                        }
                    }
                }
            } else {
                if (itemMatcher == Ingredient.EMPTY) {
                    return itemHandler.extractItem(slot, amount == null ? 64 : amount, false);
                } else {
                    if (!isEqualAdvanced(itemMatcher, itemHandler.getStackInSlot(slot), strictnbt)) {
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

    public static boolean isEqualAdvanced(Ingredient itemMatcher, ItemStack stack, boolean strictnbt) {
        if (!stack.isEmpty() && itemMatcher.test(stack)) {
//            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, itemMatcher)) {
            if (strictnbt) {
                // @todo 1.15
//                if (itemMatcher.hasTag() || stack.hasTag()) {
//                    String t1 = itemMatcher.serializeNBT().toString();
//                    String t2 = stack.serializeNBT().toString();
//                    if (t1.equalsIgnoreCase(t2)) {
//                        return true;
//                    }
//                } else {
//                    return true;
//                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static ItemStack tryExtractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                           @Nullable Integer amount, boolean routable, boolean oredict,
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
            int cnt = scanner.countItems(itemMatcher, routable, null);
            if (cnt > 0) {
                // @todo 1.15 VERY WRONG
//                ItemStack copy = itemMatcher.copy();
//                int amount1 = Math.min(cnt, amount == null ? itemMatcher.getMaxStackSize() : amount);
//                if (amount1 <= 0) {
//                    copy.setCount(0);
//                } else {
//                    copy.setCount(amount1);
//                }
//                return copy;
            }
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
                if (cnt <= 0) {
                    copy.setCount(0);
                } else {
                    copy.setCount(cnt);
                }
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
