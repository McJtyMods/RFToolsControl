package mcjty.rftoolscontrol.logic;

import mcjty.rftools.api.storage.IStorageScanner;
import mcjty.rftoolscontrol.api.parameters.BlockSide;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public static ItemStack extractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                        @Nullable Integer amount, boolean routable, boolean oredict, @Nullable ItemStack itemMatcher,
                                        @Nullable Integer slot) {
        if (itemHandler != null) {
            // @todo implement oredict here
            if (slot == null) {
                if (itemMatcher == null) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && (amount == null || amount <= stack.stackSize)) {
                            return itemHandler.extractItem(i, amount == null ? 64 : amount, false);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                            return itemHandler.extractItem(i, amount == null ? itemMatcher.getMaxStackSize() : amount, false);
                        }
                    }
                }
            } else {
                if (itemMatcher == null) {
                    return itemHandler.extractItem(slot, amount == null ? 64 : amount, false);
                } else {
                    if (!ItemStack.areItemsEqual(itemMatcher, itemHandler.getStackInSlot(slot))) {
                        return null;
                    }
                    return itemHandler.extractItem(slot, amount == null ? itemMatcher.getMaxStackSize() : amount, false);
                }
            }
        } else if (scanner != null) {
            return scanner.requestItem(itemMatcher, amount == null ? itemMatcher.getMaxStackSize() : amount, routable, oredict);
        }
        return null;
    }

    @Nullable
    public static ItemStack tryExtractItem(@Nullable IItemHandler itemHandler, @Nullable IStorageScanner scanner,
                                           @Nullable Integer amount, boolean routable, boolean oredict,
                                           @Nullable ItemStack itemMatcher,
                                           @Nullable Integer slot) {

        if (itemHandler != null) {
            if (slot == null) {
                if (itemMatcher == null) {
                    // Just find the first available stack
                    for (int i = 0 ; i < itemHandler.getSlots() ; i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && (amount == null || amount <= stack.stackSize)) {
                            return itemHandler.extractItem(i, amount == null ? 64 : amount, true);
                        }
                    }
                } else {
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                            return itemHandler.extractItem(i, amount == null ? itemMatcher.getMaxStackSize() : amount, true);
                        }
                    }
                }
            } else {
                if (itemMatcher == null) {
                    return itemHandler.extractItem(slot, amount == null ? 64 : amount, true);
                } else {
                    if (!ItemStack.areItemsEqual(itemMatcher, itemHandler.getStackInSlot(slot))) {
                        return null;
                    }
                    return itemHandler.extractItem(slot, amount == null ? itemMatcher.getMaxStackSize() : amount, true);
                }
            }
        }
        if (scanner != null) {
            int cnt = scanner.countItems(itemMatcher, routable, oredict);
            if (cnt > 0) {
                ItemStack copy = itemMatcher.copy();
                copy.stackSize = Math.min(cnt, amount == null ? itemMatcher.getMaxStackSize() : amount);
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
        EnumFacing side = getSideFromChar(s.charAt(indexOf-1));
        if (side == null) {
            // Side == null is invalid for Inventory
            return null;
        }
        EnumFacing intSide = getSideFromChar(s.charAt(indexOf+1));

        int indexSpace = s.lastIndexOf(' ');
        if (indexSpace <= 0) {
            return new Inventory(null, side, intSide);
        }

        return new Inventory(s.substring(0, indexSpace), side, intSide);
    }

    public static EnumFacing getSideFromChar(char is) {
        switch (is) {
            case '*': return null;
            case 'D': return EnumFacing.DOWN;
            case 'U': return EnumFacing.UP;
            case 'W': return EnumFacing.WEST;
            case 'E': return EnumFacing.EAST;
            case 'S': return EnumFacing.SOUTH;
            case 'N': return EnumFacing.NORTH;
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
        EnumFacing side = getSideFromChar(s.charAt(s.length()-1));
        int indexOf = s.lastIndexOf(' ');
        if (indexOf <= 0) {
            return new BlockSide(null, side);
        }
        return new BlockSide(s.substring(0, indexOf), side);
    }
}
