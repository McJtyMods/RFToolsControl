package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

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
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "true" : "false";
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).getDisplayName();
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
        } else {
            return false;
        }
    }

    public static int convertToInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
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

    public static Integer convertToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
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
