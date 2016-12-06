package mcjty.rftoolscontrol.logic.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

public class ParameterTypeTools {

    public static String stringRepresentation(ParameterType type, ParameterValue value) {
        if (value.isVariable()) {
            return "V:" + value.getVariableIndex();
        } else if (value.isFunction()) {
            return "F:" + value.getFunction().getName();
        } else if (value.getValue() == null) {
            return "";
        } else {
            return stringRepresentationInternal(type, value.getValue());
        }
    }

    private static String stringRepresentationInternal(ParameterType type, Object value) {
        switch (type) {
            case PAR_STRING:
                return (String) value;
            case PAR_INTEGER:
                return Integer.toString((Integer) value);
            case PAR_FLOAT:
                return Float.toString((Float) value);
            case PAR_SIDE:
                return ((BlockSide) value).getStringRepresentation();
            case PAR_BOOLEAN:
                return ((Boolean) value) ? "true" : "false";
            case PAR_INVENTORY:
                return ((Inventory) value).getStringRepresentation();
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) value;
                return StringUtils.left(itemStack.getDisplayName(), 10);
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                return StringUtils.left(fluidStack.getLocalizedName(), 10);
            case PAR_EXCEPTION:
                ExceptionType exception = (ExceptionType) value;
                return exception.getCode();
            case PAR_TUPLE:
                return value.toString();
        }
        return "?";
    }

    public static JsonElement writeToJson(ParameterType type, ParameterValue value) {
        JsonObject jsonObject = new JsonObject();
        if (value.isVariable()) {
            jsonObject.add("var", new JsonPrimitive(value.getVariableIndex()));
        } else if (value.isFunction()) {
            jsonObject.add("fun", new JsonPrimitive(value.getFunction().getId()));
        } else if (value.getValue() == null) {
            jsonObject.add("null", new JsonPrimitive(true));
        } else {
            writeToJsonInternal(jsonObject, type, value.getValue());
        }

        return jsonObject;
    }

    public static ParameterValue readFromJson(ParameterType type, JsonObject object) {
        if (object.has("var")) {
            return ParameterValue.variable(object.get("var").getAsInt());
        } else if (object.has("fun")) {
            return ParameterValue.function(Functions.FUNCTIONS.get(object.get("fun").getAsString()));
        } else if (object.has("null")) {
            return ParameterValue.constant(null);
        } else {
            return readFromJsonInternal(object, type);
        }
    }

    public static void writeToNBT(NBTTagCompound tag, ParameterType type, ParameterValue value) {
        if (value.isVariable()) {
            tag.setInteger("varIdx", value.getVariableIndex());
        } else if (value.isFunction()) {
            tag.setString("funId", value.getFunction().getId());
        } else if (value.getValue() == null) {
            // No value
            tag.setBoolean("null", true);
        } else {
            writeToNBTInternal(tag, type, value.getValue());
        }
    }

    public static ParameterValue readFromNBT(NBTTagCompound tag, ParameterType type) {
        if (tag.hasKey("varIdx")) {
            return ParameterValue.variable(tag.getInteger("varIdx"));
        } else if (tag.hasKey("funId")) {
            return ParameterValue.function(Functions.FUNCTIONS.get(tag.getString("funId")));
        } else if (tag.hasKey("null")) {
            return ParameterValue.constant(null);
        } else {
            return readFromNBTInternal(tag, type);
        }
    }

    private static ParameterValue readFromNBTInternal(NBTTagCompound tag, ParameterType type) {
        switch (type) {
            case PAR_STRING:
                return ParameterValue.constant(tag.getString("v"));
            case PAR_INTEGER:
                return ParameterValue.constant(tag.getInteger("v"));
            case PAR_FLOAT:
                return ParameterValue.constant(tag.getFloat("v"));
            case PAR_SIDE:
                int v = tag.getInteger("v");
                EnumFacing facing = v == -1 ? null : EnumFacing.values()[v];
                String node = tag.getString("node");
                return ParameterValue.constant(new BlockSide(node, facing));
            case PAR_BOOLEAN:
                return ParameterValue.constant(tag.getBoolean("v"));
            case PAR_INVENTORY:
                EnumFacing side = EnumFacing.values()[tag.getInteger("side")];
                String name = null;
                EnumFacing intSide = null;
                if (tag.hasKey("nodeName")) {
                    name = tag.getString("nodeName");
                }
                if (tag.hasKey("intSide")) {
                    intSide = EnumFacing.values()[tag.getInteger("intSide")];
                }
                return ParameterValue.constant(new Inventory(name, side, intSide));
            case PAR_ITEM:
                if (tag.hasKey("item")) {
                    NBTTagCompound tc = (NBTTagCompound) tag.getTag("item");
                    ItemStack stack = ItemStackTools.loadFromNBT(tc);
                    // Fix for 1.10 0-sized stacks
                    if (ItemStackTools.getStackSize(stack) == 0) {
                        ItemStackTools.setStackSize(stack, 1);
                    }
                    return ParameterValue.constant(stack);
                }
                return ParameterValue.constant(ItemStackTools.getEmptyStack());
            case PAR_FLUID:
                if (tag.hasKey("fluid")) {
                    NBTTagCompound tc = (NBTTagCompound) tag.getTag("fluid");
                    FluidStack stack = FluidStack.loadFluidStackFromNBT(tc);
                    return ParameterValue.constant(stack);
                }
                return ParameterValue.constant(null);
            case PAR_EXCEPTION:
                String code = tag.getString("code");
                return ParameterValue.constant(ExceptionType.getExceptionForCode(code));
            case PAR_TUPLE:
                return ParameterValue.constant(new Tuple(tag.getInteger("x"), tag.getInteger("y")));
        }
        return ParameterValue.constant(null);
    }

    private static void writeToNBTInternal(NBTTagCompound tag, ParameterType type, Object value) {
        switch (type) {
            case PAR_STRING:
                tag.setString("v", (String) value);
                break;
            case PAR_INTEGER:
                tag.setInteger("v", (Integer) value);
                break;
            case PAR_FLOAT:
                tag.setFloat("v", (Float) value);
                break;
            case PAR_SIDE:
                BlockSide side = (BlockSide) value;
                tag.setInteger("v", side.getSide() == null ? -1 : side.getSide().ordinal());
                tag.setString("node", side.getNodeName() == null ? "" : side.getNodeName());
                break;
            case PAR_BOOLEAN:
                tag.setBoolean("v", (Boolean) value);
                break;
            case PAR_INVENTORY:
                Inventory inv = (Inventory) value;
                if (inv.getNodeName() != null) {
                    tag.setString("nodeName", inv.getNodeName());
                }
                tag.setInteger("side", inv.getSide().ordinal());
                if (inv.getIntSide() != null) {
                    tag.setInteger("intSide", inv.getIntSide().ordinal());
                }
                break;
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) value;
                NBTTagCompound tc = new NBTTagCompound();
                itemStack.writeToNBT(tc);
                tag.setTag("item", tc);
                break;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                NBTTagCompound fluidTc = new NBTTagCompound();
                fluidStack.writeToNBT(fluidTc);
                tag.setTag("fluid", fluidTc);
                break;
            case PAR_EXCEPTION:
                ExceptionType exception = (ExceptionType) value;
                tag.setString("code", exception.getCode());
                break;
            case PAR_TUPLE:
                tag.setInteger("x", ((Tuple) value).getX());
                tag.setInteger("y", ((Tuple) value).getY());
                break;
        }
    }

    private static void writeToJsonInternal(JsonObject object, ParameterType type, Object value) {
        switch (type) {
            case PAR_STRING:
                object.add("v", new JsonPrimitive((String) value));
                break;
            case PAR_INTEGER:
                object.add("v", new JsonPrimitive((Integer) value));
                break;
            case PAR_FLOAT:
                object.add("v", new JsonPrimitive((Float) value));
                break;
            case PAR_SIDE:
                BlockSide side = (BlockSide) value;
                if (side.getSide() != null) {
                    object.add("side", new JsonPrimitive(side.getSide().getName()));
                }
                if (side.getNodeName() != null) {
                    object.add("node", new JsonPrimitive(side.getNodeName()));
                }
                break;
            case PAR_BOOLEAN:
                object.add("v", new JsonPrimitive((Boolean) value));
                break;
            case PAR_INVENTORY:
                Inventory inv = (Inventory) value;
                object.add("side", new JsonPrimitive(inv.getSide().getName()));
                if (inv.getIntSide() != null) {
                    object.add("intside", new JsonPrimitive(inv.getIntSide().getName()));
                }
                if (inv.getNodeName() != null) {
                    object.add("node", new JsonPrimitive(inv.getNodeName()));
                }
                break;
            case PAR_ITEM:
                ItemStack item = (ItemStack) value;
                object.add("item", new JsonPrimitive(item.getItem().getRegistryName().toString()));
                if (ItemStackTools.getStackSize(item) != 1) {
                    object.add("amount", new JsonPrimitive(ItemStackTools.getStackSize(item)));
                }
                object.add("meta", new JsonPrimitive(item.getItemDamage()));
                if (item.hasTagCompound()) {
                    String string = item.getTagCompound().toString();
                    object.add("nbt", new JsonPrimitive(string));
                }
                break;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                object.add("fluid", new JsonPrimitive(fluidStack.getFluid().getName()));
                object.add("amount", new JsonPrimitive(fluidStack.amount));
                if (fluidStack.tag != null) {
                    object.add("nbt", new JsonPrimitive(fluidStack.tag.toString()));
                }
                break;
            case PAR_EXCEPTION:
                ExceptionType exception = (ExceptionType) value;
                object.add("code", new JsonPrimitive(exception.getCode()));
                break;
            case PAR_TUPLE:
                object.add("x", new JsonPrimitive(((Tuple) value).getX()));
                object.add("y", new JsonPrimitive(((Tuple) value).getY()));
                break;
        }
    }

    private static ParameterValue readFromJsonInternal(JsonObject object, ParameterType type) {
        switch (type) {
            case PAR_STRING:
                return ParameterValue.constant(object.get("v").getAsString());
            case PAR_INTEGER:
                return ParameterValue.constant(object.get("v").getAsInt());
            case PAR_FLOAT:
                return ParameterValue.constant(object.get("v").getAsFloat());
            case PAR_SIDE: {
                EnumFacing side = object.has("side") ? EnumFacing.byName(object.get("side").getAsString()) : null;
                String node = object.has("node") ? object.get("node").getAsString() : null;
                return ParameterValue.constant(new BlockSide(node, side));
            }
            case PAR_BOOLEAN:
                return ParameterValue.constant(object.get("v").getAsBoolean());
            case PAR_INVENTORY: {
                EnumFacing side = EnumFacing.byName(object.get("side").getAsString());
                EnumFacing intSide = object.has("intside") ? EnumFacing.byName(object.get("intside").getAsString()) : null;
                String node = object.has("node") ? object.get("node").getAsString() : null;
                return ParameterValue.constant(new Inventory(node, side, intSide));
            }
            case PAR_ITEM: {
                String itemReg = object.get("item").getAsString();
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemReg));
                int amount = object.has("amount") ? object.get("amount").getAsInt() : 1;
                int meta = object.get("meta").getAsInt();
                ItemStack stack = new ItemStack(item, amount, meta);
                if (object.has("nbt")) {
                    String nbt = object.get("nbt").getAsString();
                    NBTTagCompound tagCompound = null;
                    try {
                        tagCompound = JsonToNBT.getTagFromJson(nbt);
                    } catch (NBTException e) {
                        // @todo What to do?
                    }
                    stack.setTagCompound(tagCompound);
                }
                return ParameterValue.constant(stack);
            }
            case PAR_FLUID: {
                String fluidName = object.get("fluid").getAsString();
                int amount = object.get("amount").getAsInt();
                FluidStack fluidStack = new FluidStack(FluidRegistry.getFluid(fluidName), amount);
                if (object.has("nbt")) {
                    String nbt = object.get("nbt").getAsString();
                    try {
                        fluidStack.tag = JsonToNBT.getTagFromJson(nbt);
                    } catch (NBTException e) {
                        // @todo What to do?
                    }
                }
                return ParameterValue.constant(fluidStack);
            }
            case PAR_EXCEPTION:
                String code = object.get("code").getAsString();
                return ParameterValue.constant(ExceptionType.getExceptionForCode(code));
            case PAR_TUPLE:
                return ParameterValue.constant(new Tuple(object.get("x").getAsInt(), object.get("y").getAsInt()));
        }
        return null;
    }

}
