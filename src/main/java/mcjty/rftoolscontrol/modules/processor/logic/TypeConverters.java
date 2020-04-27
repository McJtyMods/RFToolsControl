package mcjty.rftoolscontrol.modules.processor.logic;

import mcjty.lib.varia.FluidTools;
import mcjty.rftoolsbase.api.control.parameters.*;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TypeConverters {

    public static float convertToFloat(IParameter value) {
        if (value == null) {
            return 0.0f;
        }
        if (!value.isSet()) {
            return 0.0f;
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return Float.parseFloat((String) v);
            case PAR_INTEGER:
                return ((Integer) v).floatValue();
            case PAR_LONG:
                return ((Long) v).floatValue();
            case PAR_FLOAT:
                return (Float) v;
            case PAR_NUMBER:
                return castToFloat(v);
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1.0f : 0.0f;
            case PAR_ITEM:
                return ((ItemStack) v).getCount();
            case PAR_FLUID:
                return ((FluidStack) v).getAmount();
            case PAR_INVENTORY:
            case PAR_SIDE:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_VECTOR:
                break;
        }
        return 0.0f;
    }

    @Nullable
    public static FluidStack convertToFluid(IParameter parameter) {
        if (parameter == null) {
            return null;
        }
        return convertToFluid(parameter.getParameterType(), parameter.getParameterValue().getValue());
    }

    @Nullable
    public static FluidStack convertToFluid(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                return new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation((String) v)), 1);
            case PAR_FLUID:
                return (FluidStack) v;
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) v;
                return FluidUtil.getFluidContained(itemStack).map(f -> f).orElse(FluidStack.EMPTY);
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_SIDE:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_VECTOR:
                break;
        }
        return null;
    }

    @Nonnull
    public static ItemStack convertToItem(IParameter value) {
        if (value == null) {
            return ItemStack.EMPTY;
        }
        return convertToItem(value.getParameterType(), value.getParameterValue().getValue());
    }

    @Nonnull
    public static ItemStack convertToItem(ParameterType type, Object v) {
        if (v == null) {
            return ItemStack.EMPTY;
        }
        switch (type) {
            case PAR_STRING:
                return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) v)), 1);
            case PAR_ITEM:
                return (ItemStack) v;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) v;
                return FluidTools.convertFluidToBucket(fluidStack);
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_SIDE:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_VECTOR:
                break;
        }
        return ItemStack.EMPTY;
    }


    @Nullable
    public static Inventory convertToInventory(IParameter par) {
        if (par == null) {
            return null;
        }
        return convertToInventory(par.getParameterType(), par.getParameterValue().getValue());
    }

    @Nullable
    public static Inventory convertToInventory(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_INVENTORY:
                return (Inventory) v;
            case PAR_SIDE: {
                BlockSide bs = (BlockSide) v;
                if (bs.getSide() == null) {
                    throw new ProgException(ExceptionType.EXCEPT_BADPARAMETERS);
                }
                return new Inventory(bs.getNodeName(), bs.getSide(), null);
            }
            case PAR_STRING:
                return LogicInventoryTools.inventoryFromString(v.toString());
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_BOOLEAN:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_VECTOR:
                break;
        }
        return null;
    }

    @Nullable
    public static BlockSide convertToSide(IParameter par) {
        if (par == null) {
            return null;
        }
        return convertToSide(par.getParameterType(), par.getParameterValue().getValue());
    }

    @Nullable
    public static BlockSide convertToSide(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_SIDE:
                return (BlockSide) v;
            case PAR_INVENTORY:
                return (Inventory) v;
            case PAR_STRING:
                return LogicInventoryTools.blockSideFromString(v.toString());
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_BOOLEAN:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_VECTOR:
                break;
        }
        return null;
    }

    @Nullable
    public static Tuple convertToTuple(IParameter value) {
        if (value == null || value.getParameterValue() == null) {
            return null;
        }
        return convertToTuple(value.getParameterType(), value.getParameterValue().getValue());
    }

    @Nullable
    public static Tuple convertToTuple(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_TUPLE:
                return (Tuple) v;
            case PAR_VECTOR:
                // @todo? Smart conversion here?
                break;
            case PAR_STRING: {
                String s = (String) v;
                String[] split = StringUtils.split(s, ',');
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                return new Tuple(x, y);
            }
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_SIDE:
                break;
        }
        return null;
    }

    @Nullable
    public static List<Parameter> convertToVector(IParameter value) {
        if (value == null || value.getParameterValue() == null) {
            return null;
        }
        return convertToVector(value.getParameterType(), value.getParameterValue().getValue());
    }

    @Nullable
    public static List<Parameter> convertToVector(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_VECTOR:
                return (List<Parameter>) v;
            case PAR_STRING:
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_SIDE:
            case PAR_TUPLE:
                break;
        }
        return null;
    }

    public static boolean convertToBool(IParameter value) {
        if (value == null) {
            return false;
        }
        return convertToBool(value.getParameterType(), value.getParameterValue().getValue());
    }

    public static boolean convertToBool(ParameterType type, Object v) {
        if (v == null) {
            return false;
        }
        switch (type) {
            case PAR_STRING:
                return !((String) v).isEmpty();
            case PAR_INTEGER:
                return ((Integer) v) != 0;
            case PAR_LONG:
                return ((Long) v) != 0;
            case PAR_FLOAT:
                return ((Float) v) != 0;
            case PAR_NUMBER:
                return castToInt(v) != 0;
            case PAR_BOOLEAN:
                return (Boolean) v;
            case PAR_TUPLE:
                return ((Tuple) v).getX() != 0 || ((Tuple) v).getY() != 0;
            case PAR_VECTOR:
                return !((List<?>)v).isEmpty();
            case PAR_ITEM:
                return !((ItemStack) v).isEmpty();
            case PAR_SIDE:
            case PAR_INVENTORY:
            case PAR_FLUID:
            case PAR_EXCEPTION:
                return true;
        }
        return false;
    }

    public static int convertToInt(IParameter value) {
        if (value == null) {
            return 0;
        }
        Integer integer = convertToInteger(value.getParameterType(), value.getParameterValue().getValue());
        if (integer == null) {
            return 0;
        }
        return integer;
    }

    public static long convertToLong(IParameter value) {
        if (value == null) {
            return 0;
        }
        Long l = convertToLong(value.getParameterType(), value.getParameterValue().getValue());
        if (l == null) {
            return 0;
        }
        return l;
    }

    @Nonnull
    public static Number convertToNumber(IParameter value) {
        if (value == null) {
            return 0;
        }
        Number l = convertToNumber(value.getParameterType(), value.getParameterValue().getValue());
        if (l == null) {
            return 0;
        }
        return l;
    }

    @Nullable
    public static Integer convertToInteger(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                String s = (String) v;
                if (s.startsWith("$")) {
                    return (int) Long.parseLong(s.substring(1), 16);
                } else {
                    return Integer.parseInt(s);
                }
            case PAR_INTEGER:
                return (Integer) v;
            case PAR_LONG:
                return ((Long) v).intValue();
            case PAR_FLOAT:
                return ((Float) v).intValue();
            case PAR_NUMBER:
                return castToInt(v);
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1 : 0;
            case PAR_ITEM:
                return ((ItemStack) v).getCount();
            case PAR_FLUID:
                return ((FluidStack) v).getAmount();
            case PAR_VECTOR:
                return ((List<?>)v).size();
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_SIDE:
            case PAR_INVENTORY:
                break;
        }
        return null;
    }

    @Nullable
    public static Long convertToLong(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                String s = (String) v;
                if (s.startsWith("$")) {
                    return Long.parseLong(s.substring(1), 16);
                } else {
                    return Long.parseLong(s);
                }
            case PAR_INTEGER:
                return ((Integer) v).longValue();
            case PAR_LONG:
                return (Long) v;
            case PAR_FLOAT:
                return ((Float) v).longValue();
            case PAR_NUMBER:
                return castToLong(v);
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1L : 0L;
            case PAR_ITEM:
                return Long.valueOf(((ItemStack) v).getCount());
            case PAR_FLUID:
                return Long.valueOf(((FluidStack) v).getAmount());
            case PAR_VECTOR:
                return Long.valueOf(((List<?>)v).size());
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_SIDE:
            case PAR_INVENTORY:
                break;
        }
        return null;
    }

    @Nullable
    public static Number convertToNumber(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                String s = (String) v;
                if (s.startsWith("$")) {
                    return Long.parseLong(s.substring(1), 16);
                } else if (s.contains(",") || s.contains(".") || s.contains("e") || s.contains("E")) {
                    return Double.parseDouble(s);
                } else {
                    return Long.parseLong(s);
                }
            case PAR_INTEGER:
            case PAR_LONG:
            case PAR_FLOAT:
            case PAR_NUMBER:
                return (Number) v;
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1L : 0L;
            case PAR_ITEM:
                return Integer.valueOf(((ItemStack) v).getCount());
            case PAR_FLUID:
                return Integer.valueOf(((FluidStack) v).getAmount());
            case PAR_VECTOR:
                return Integer.valueOf(((List<?>)v).size());
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_SIDE:
            case PAR_INVENTORY:
                break;
        }
        return null;
    }

    @Nonnull
    public static String convertToString(IParameter value) {
        if (value == null) {
            return "";
        }
        String s = convertToString(value.getParameterType(), value.getParameterValue().getValue());
        if (s == null) {
            return "";
        }
        return s;
    }

    @Nullable
    public static String convertToString(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                return (String) v;
            case PAR_INTEGER:
                return Integer.toString((Integer) v);
            case PAR_LONG:
                return Long.toString((Long) v);
            case PAR_FLOAT:
                return Float.toString((Float) v);
            case PAR_NUMBER: {
                if (v instanceof Integer) {
                    return Integer.toString((Integer) v);
                } else if (v instanceof Long) {
                    return Long.toString((Long) v);
                } else if (v instanceof Float) {
                    return Float.toString((Float) v);
                } else if (v instanceof Double) {
                    return Double.toString((Double) v);
                } else {
                    return "?";
                }
            }
            case PAR_BOOLEAN:
                return ((Boolean) v) ? "true" : "false";
            case PAR_ITEM:
                if (!((ItemStack) v).isEmpty()) {
                    return ((ItemStack) v).getItem().getRegistryName().toString();
                } else {
                    return null;
                }
            case PAR_FLUID:
                return ((FluidStack) v).getFluid().getRegistryName().toString();
            case PAR_INVENTORY:
                return LogicInventoryTools.inventoryToString((Inventory) v);
            case PAR_SIDE:
                return LogicInventoryTools.blockSideToString((BlockSide) v);
            case PAR_EXCEPTION:
                return ((ExceptionType) v).getCode();
            case PAR_TUPLE:
                return v.toString();
            case PAR_VECTOR:
                return vectorToString((List<Parameter>) v, 50);
        }
        return null;
    }

    private static String vectorToString(List<Parameter> v, int max) {
        StringBuilder builder = new StringBuilder('[');
        List<Parameter> vector = v;
        boolean first = true;
        for (Parameter par : vector) {
            if (!first) {
                builder.append(',');
            }
            if (builder.length() >= max) {
                builder.append("...");
                break;
            }
            builder.append(convertToString(par));
            first = false;
        }
        builder.append(']');
        return builder.toString();
    }

    public static int castToInt(Object value) {
        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else {
            return 0;
        }
    }

    public static long castToLong(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (long) value;
        } else if (value instanceof Float) {
            return ((Float) value).longValue();
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        } else {
            return 0;
        }
    }

    public static float castToFloat(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).floatValue();
        } else if (value instanceof Float) {
            return (float) value;
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else {
            return 0;
        }
    }

    public static double castToDouble(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Double) {
            return (double) value;
        } else {
            return 0;
        }
    }

    public static String castNumberToString(Object value) {
        if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        } else if (value instanceof Long) {
            return Long.toString((Long) value);
        } else if (value instanceof Float) {
            return Float.toString((Float) value);
        } else if (value instanceof Double) {
            return Double.toString((Double) value);
        } else {
            return "?";
        }
    }

    public static String getNumberType(Object value) {
        if (value instanceof Integer) {
            return "I";
        } else if (value instanceof Long) {
            return "L";
        } else if (value instanceof Float) {
            return "F";
        } else if (value instanceof Double) {
            return "D";
        } else {
            return "?";
        }
    }
}
