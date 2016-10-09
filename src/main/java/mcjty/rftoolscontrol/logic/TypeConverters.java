package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.api.parameters.BlockSide;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TypeConverters {

    public static String convertToString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        } else if (value instanceof Float) {
            return Float.toString((Float) value);
        } else if (value instanceof EnumFacing) {
            return ((EnumFacing) value).getName();
        } else if (value instanceof Inventory) {
            return InventoryTools.inventoryToString((Inventory) value);
        } else if (value instanceof BlockSide) {
            return InventoryTools.blockSideToString((BlockSide) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "true" : "false";
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).getItem().getRegistryName().toString();
        } else if (value instanceof ExceptionType) {
            return ((ExceptionType) value).getCode();
        } else {
            return "";
        }
    }

    public static boolean convertToBool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        } else if (value instanceof String) {
            return !((String) value).isEmpty();
        } else if (value instanceof EnumFacing) {
            return true;
        } else if (value instanceof ExceptionType) {
            return true;
        } else if (value instanceof Inventory) {
            return true;
        } else if (value instanceof BlockSide) {
            return true;
        } else if (value instanceof ItemStack) {
            return true;
        } else {
            return false;
        }
    }

    public static int convertToInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if (s.startsWith("$")) {
                return (int) Long.parseLong(s.substring(1), 16);
            } else {
                return Integer.parseInt(s);
            }
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).stackSize;
        } else {
            return 0;
        }
    }

    public static ItemStack convertToItem(Object value) {
        if (value instanceof ItemStack) {
            return (ItemStack) value;
        } else if (value instanceof String) {
            return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) value)), 1, 0);
        } else {
            return null;
        }
    }

    public static BlockSide convertToSide(Object value) {
        if (value instanceof BlockSide) {
            return (BlockSide) value;
        } else if (value instanceof String) {
            return InventoryTools.blockSideFromString((String) value);
        } else {
            return null;
        }
    }

    public static Inventory convertToInventory(Object value) {
        if (value instanceof Inventory) {
            return (Inventory) value;
        } else if (value instanceof BlockSide) {
            BlockSide s = (BlockSide) value;
            if (s.getSide() == null) {
                return null;
            }
            return new Inventory(s.getNodeName(), s.getSide(), null);
        } else if (value instanceof String) {
            return InventoryTools.inventoryFromString((String) value);
        } else {
            return null;
        }
    }

    public static float convertToFloat(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof String) {
            return Float.parseFloat((String) value);
        } else if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0f : 0.0f;
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).stackSize;
        } else {
            return 0.0f;
        }
    }

    public static Integer convertToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            String s = (String) value;
            if (s.startsWith("$")) {
                return (int) Long.parseLong(s.substring(1), 16);
            } else {
                return Integer.parseInt(s);
            }
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).stackSize;
        } else {
            return null;
        }
    }


}
