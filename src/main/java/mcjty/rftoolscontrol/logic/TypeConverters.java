package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.running.ProgException;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TypeConverters {

    public static float convertToFloat(Parameter value) {
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
            case PAR_FLOAT:
                return (Float) v;
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1.0f : 0.0f;
            case PAR_ITEM:
                return ((ItemStack) v).stackSize;
            case PAR_FLUID:
                return ((FluidStack) v).amount;
            case PAR_INVENTORY:
            case PAR_SIDE:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return 0.0f;
    }

    @Nullable
    public static FluidStack convertToFluid(Parameter parameter) {
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
                return new FluidStack(FluidRegistry.getFluid((String) v), 1);
            case PAR_FLUID:
                return (FluidStack) v;
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) v;
                return FluidUtil.getFluidContained(itemStack);
            case PAR_INTEGER:
            case PAR_FLOAT:
            case PAR_SIDE:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return null;
    }

    @Nullable
    public static ItemStack convertToItem(Parameter value) {
        if (value == null) {
            return null;
        }
        return convertToItem(value.getParameterType(), value.getParameterValue().getValue());
    }

    @Nullable
    public static ItemStack convertToItem(ParameterType type, Object v) {
        if (v == null) {
            return null;
        }
        switch (type) {
            case PAR_STRING:
                return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation((String) v)), 1, 0);
            case PAR_ITEM:
                return (ItemStack) v;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) v;
                return FluidContainerRegistry.fillFluidContainer(fluidStack, new ItemStack(Items.BUCKET));
            case PAR_INTEGER:
            case PAR_FLOAT:
            case PAR_SIDE:
            case PAR_BOOLEAN:
            case PAR_INVENTORY:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return null;
    }


    @Nullable
    public static Inventory convertToInventory(Parameter par) {
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
                return InventoryTools.inventoryFromString(v.toString());
            case PAR_INTEGER:
            case PAR_FLOAT:
            case PAR_BOOLEAN:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return null;
    }

    @Nullable
    public static BlockSide convertToSide(Parameter par) {
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
                return InventoryTools.blockSideFromString(v.toString());
            case PAR_INTEGER:
            case PAR_FLOAT:
            case PAR_BOOLEAN:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
            case PAR_TUPLE:
                break;
        }
        return null;
    }

    @Nullable
    public static Tuple convertToTuple(Parameter value) {
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
            case PAR_STRING: {
                String s = (String) v;
                String[] split = StringUtils.split(s, ',');
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                return new Tuple(x, y);
            }
            case PAR_INTEGER:
            case PAR_FLOAT:
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

    public static boolean convertToBool(Parameter value) {
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
            case PAR_FLOAT:
                return ((Float) v) != 0;
            case PAR_BOOLEAN:
                return (Boolean) v;
            case PAR_TUPLE:
                return ((Tuple) v).getX() != 0 || ((Tuple) v).getY() != 0;
            case PAR_SIDE:
            case PAR_INVENTORY:
            case PAR_ITEM:
            case PAR_FLUID:
            case PAR_EXCEPTION:
                return true;
        }
        return false;
    }

    public static int convertToInt(Parameter value) {
        if (value == null) {
            return 0;
        }
        Integer integer = convertToInteger(value.getParameterType(), value.getParameterValue().getValue());
        if (integer == null) {
            return 0;
        }
        return integer;
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
            case PAR_FLOAT:
                return ((Float) v).intValue();
            case PAR_BOOLEAN:
                return ((Boolean) v) ? 1 : 0;
            case PAR_ITEM:
                return ((ItemStack) v).stackSize;
            case PAR_FLUID:
                return ((FluidStack) v).amount;
            case PAR_EXCEPTION:
            case PAR_TUPLE:
            case PAR_SIDE:
            case PAR_INVENTORY:
                break;
        }
        return null;
    }


    @Nonnull
    public static String convertToString(Parameter value) {
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
            case PAR_FLOAT:
                return Float.toString((Float) v);
            case PAR_BOOLEAN:
                return ((Boolean) v) ? "true" : "false";
            case PAR_ITEM:
                return ((ItemStack) v).getItem().getRegistryName().toString();
            case PAR_FLUID:
                return ((FluidStack) v).getFluid().getName();
            case PAR_INVENTORY:
                return InventoryTools.inventoryToString((Inventory) v);
            case PAR_SIDE:
                return InventoryTools.blockSideToString((BlockSide) v);
            case PAR_EXCEPTION:
                return ((ExceptionType) v).getCode();
            case PAR_TUPLE:
                return v.toString();
        }
        return null;
    }

}
