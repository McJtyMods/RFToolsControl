package mcjty.rftoolscontrol.modules.processor.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.rftoolsbase.api.control.parameters.*;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            case PAR_LONG:
                return Long.toString((Long) value);
            case PAR_FLOAT:
                return Float.toString((Float) value);
            case PAR_NUMBER: {
                String s = TypeConverters.castNumberToString(value);
                return s + " (" + TypeConverters.getNumberType(value) + ")";
            }
            case PAR_SIDE:
                return ((BlockSide) value).getStringRepresentation();
            case PAR_BOOLEAN:
                return ((Boolean) value) ? "true" : "false";
            case PAR_INVENTORY:
                return ((Inventory) value).getStringRepresentation();
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) value;
                return StringUtils.left(itemStack.getHoverName().getString() /* was getFormattedText() */, 10);
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                return StringUtils.left(fluidStack.getDisplayName().getString() /* was getFormattedText() */, 10);
            case PAR_EXCEPTION:
                ExceptionType exception = (ExceptionType) value;
                return exception.getCode();
            case PAR_TUPLE:
                return value.toString();
            case PAR_VECTOR:
                return "[" + ((List<?>)value).size() + "]";
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

    public static void writeToNBT(CompoundTag tag, ParameterType type, ParameterValue value) {
        if (value.isVariable()) {
            tag.putInt("varIdx", value.getVariableIndex());
        } else if (value.isFunction()) {
            tag.putString("funId", value.getFunction().getId());
        } else if (value.getValue() == null) {
            // No value
            tag.putBoolean("null", true);
        } else {
            writeToNBTInternal(tag, type, value.getValue());
        }
    }

    public static ParameterValue readFromNBT(CompoundTag tag, ParameterType type) {
        if (tag.contains("varIdx")) {
            return ParameterValue.variable(tag.getInt("varIdx"));
        } else if (tag.contains("funId")) {
            return ParameterValue.function(Functions.FUNCTIONS.get(tag.getString("funId")));
        } else if (tag.contains("null")) {
            return ParameterValue.constant(null);
        } else {
            return readFromNBTInternal(tag, type);
        }
    }

    private static ParameterValue readFromNBTInternal(CompoundTag tag, ParameterType type) {
        switch (type) {
            case PAR_STRING:
                return ParameterValue.constant(tag.getString("v"));
            case PAR_INTEGER:
                return ParameterValue.constant(tag.getInt("v"));
            case PAR_LONG:
                return ParameterValue.constant(tag.getLong("v"));
            case PAR_FLOAT:
                return ParameterValue.constant(tag.getFloat("v"));
            case PAR_NUMBER:
                if (tag.contains("v")) {
                    return ParameterValue.constant(tag.getInt("v"));
                } else if (tag.contains("l")) {
                    return ParameterValue.constant(tag.getLong("l"));
                } else if (tag.contains("f")) {
                    return ParameterValue.constant(tag.getFloat("f"));
                } else if (tag.contains("d")) {
                    return ParameterValue.constant(tag.getDouble("d"));
                }
                break;
            case PAR_SIDE:
                int v = tag.getInt("v");
                Direction facing = v == -1 ? null : Direction.values()[v];
                String node = tag.getString("node");
                return ParameterValue.constant(new BlockSide(node, facing));
            case PAR_BOOLEAN:
                return ParameterValue.constant(tag.getBoolean("v"));
            case PAR_INVENTORY:
                Direction side = Direction.values()[tag.getInt("side")];
                String name = null;
                Direction intSide = null;
                if (tag.contains("nodeName")) {
                    name = tag.getString("nodeName");
                }
                if (tag.contains("intSide")) {
                    intSide = Direction.values()[tag.getInt("intSide")];
                }
                return ParameterValue.constant(new Inventory(name, side, intSide));
            case PAR_ITEM:
                if (tag.contains("item")) {
                    CompoundTag tc = tag.getCompound("item");
                    ItemStack stack = ItemStack.of(tc);
                    // Fix for 1.10 0-sized stacks
                    if (stack.getCount() == 0) {
                        stack.setCount(1);
                    }
                    return ParameterValue.constant(stack);
                }
                return ParameterValue.constant(ItemStack.EMPTY);
            case PAR_FLUID:
                if (tag.contains("fluid")) {
                    CompoundTag tc = tag.getCompound("fluid");
                    FluidStack stack = FluidStack.loadFluidStackFromNBT(tc);
                    return ParameterValue.constant(stack);
                }
                return ParameterValue.constant(null);
            case PAR_EXCEPTION:
                String code = tag.getString("code");
                return ParameterValue.constant(ExceptionType.getExceptionForCode(code));
            case PAR_TUPLE:
                return ParameterValue.constant(new Tuple(tag.getInt("x"), tag.getInt("y")));
            case PAR_VECTOR:
                ListTag array = tag.getList("vector", Tag.TAG_COMPOUND);
                List<Parameter> vector = new ArrayList<>();
                for (int i = 0 ; i < array.size() ; i++) {
                    vector.add(ParameterTools.readFromNBT(array.getCompound(i)));
                }
                return ParameterValue.constant(Collections.unmodifiableList(vector));
        }
        return ParameterValue.constant(null);
    }

    private static void writeToNBTInternal(CompoundTag tag, ParameterType type, Object value) {
        switch (type) {
            case PAR_STRING:
                tag.putString("v", (String) value);
                break;
            case PAR_INTEGER:
                tag.putInt("v", (Integer) value);
                break;
            case PAR_LONG:
                tag.putLong("v", (Long) value);
                break;
            case PAR_FLOAT:
                tag.putFloat("v", (Float) value);
                break;
            case PAR_NUMBER:
                if (value instanceof Integer) {
                    tag.putInt("v", (Integer) value);
                } else if (value instanceof Long) {
                    tag.putLong("l", (Long) value);
                } else if (value instanceof Float) {
                    tag.putFloat("f", (Float) value);
                } else if (value instanceof Double) {
                    tag.putDouble("d", (Double) value);
                }
                break;
            case PAR_SIDE:
                BlockSide side = (BlockSide) value;
                tag.putInt("v", side.getSide() == null ? -1 : side.getSide().ordinal());
                tag.putString("node", side.getNodeName() == null ? "" : side.getNodeName());
                break;
            case PAR_BOOLEAN:
                tag.putBoolean("v", (Boolean) value);
                break;
            case PAR_INVENTORY:
                Inventory inv = (Inventory) value;
                if (inv.getNodeName() != null) {
                    tag.putString("nodeName", inv.getNodeName());
                }
                tag.putInt("side", inv.getSide().ordinal());
                if (inv.getIntSide() != null) {
                    tag.putInt("intSide", inv.getIntSide().ordinal());
                }
                break;
            case PAR_ITEM:
                ItemStack itemStack = (ItemStack) value;
                CompoundTag tc = new CompoundTag();
                itemStack.save(tc);
                tag.put("item", tc);
                break;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                CompoundTag fluidTc = new CompoundTag();
                fluidStack.writeToNBT(fluidTc);
                tag.put("fluid", fluidTc);
                break;
            case PAR_EXCEPTION:
                ExceptionType exception = (ExceptionType) value;
                tag.putString("code", exception.getCode());
                break;
            case PAR_TUPLE:
                tag.putInt("x", ((Tuple) value).getX());
                tag.putInt("y", ((Tuple) value).getY());
                break;
            case PAR_VECTOR:
                List<Parameter> vector = (List<Parameter>) value;
                ListTag list = new ListTag();
                for (Parameter p : vector) {
                    list.add(ParameterTools.writeToNBT(p));
                }
                tag.put("vector", list);
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
            case PAR_LONG:
                object.add("v", new JsonPrimitive((Long) value));
                break;
            case PAR_FLOAT:
                object.add("v", new JsonPrimitive((Float) value));
                break;
            case PAR_NUMBER:
                if (value instanceof Integer) {
                    object.add("v", new JsonPrimitive((Integer) value));
                } else if (value instanceof Long) {
                    object.add("l", new JsonPrimitive((Long) value));
                } else if (value instanceof Float) {
                    object.add("f", new JsonPrimitive((Float) value));
                } else if (value instanceof Double) {
                    object.add("d", new JsonPrimitive((Double) value));
                }
                break;
            case PAR_SIDE:
                BlockSide side = (BlockSide) value;
                if (side.getSide() != null) {
                    object.add("side", new JsonPrimitive(side.getSide().getSerializedName()));
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
                object.add("side", new JsonPrimitive(inv.getSide().getSerializedName()));
                if (inv.getIntSide() != null) {
                    object.add("intside", new JsonPrimitive(inv.getIntSide().getSerializedName()));
                }
                if (inv.getNodeName() != null) {
                    object.add("node", new JsonPrimitive(inv.getNodeName()));
                }
                break;
            case PAR_ITEM:
                ItemStack item = (ItemStack) value;
                object.add("item", new JsonPrimitive(item.getItem().getRegistryName().toString()));
                if (item.getCount() != 1) {
                    object.add("amount", new JsonPrimitive(item.getCount()));
                }
                if (item.hasTag()) {
                    String string = item.getTag().toString();
                    object.add("nbt", new JsonPrimitive(string));
                }
                break;
            case PAR_FLUID:
                FluidStack fluidStack = (FluidStack) value;
                object.add("fluid", new JsonPrimitive(fluidStack.getFluid().getRegistryName().toString()));
                object.add("amount", new JsonPrimitive(fluidStack.getAmount()));
                if (fluidStack.hasTag()) {
                    object.add("nbt", new JsonPrimitive(fluidStack.getTag().toString()));
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
            case PAR_VECTOR:
                List<Parameter> vector = (List<Parameter>) value;
                JsonArray array = new JsonArray();
                for (Parameter p : vector) {
                    array.add(ParameterTools.getJsonElement(p));
                }
                object.add("vector", array);
                break;
        }
    }

    private static ParameterValue readFromJsonInternal(JsonObject object, ParameterType type) {
        switch (type) {
            case PAR_STRING:
                return ParameterValue.constant(object.get("v").getAsString());
            case PAR_INTEGER:
                return ParameterValue.constant(object.get("v").getAsInt());
            case PAR_LONG:
                return ParameterValue.constant(object.get("v").getAsLong());
            case PAR_FLOAT:
                return ParameterValue.constant(object.get("v").getAsFloat());
            case PAR_NUMBER:
                if (object.has("v")) {
                    return ParameterValue.constant(object.get("v").getAsInt());
                } else if (object.has("l")) {
                    return ParameterValue.constant(object.get("l").getAsLong());
                } else if (object.has("f")) {
                    return ParameterValue.constant(object.get("f").getAsFloat());
                } else if (object.has("d")) {
                    return ParameterValue.constant(object.get("d").getAsDouble());
                }
                break;
            case PAR_SIDE: {
                Direction side = object.has("side") ? Direction.byName(object.get("side").getAsString()) : null;
                String node = object.has("node") ? object.get("node").getAsString() : null;
                return ParameterValue.constant(new BlockSide(node, side));
            }
            case PAR_BOOLEAN:
                return ParameterValue.constant(object.get("v").getAsBoolean());
            case PAR_INVENTORY: {
                Direction side = Direction.byName(object.get("side").getAsString());
                Direction intSide = object.has("intside") ? Direction.byName(object.get("intside").getAsString()) : null;
                String node = object.has("node") ? object.get("node").getAsString() : null;
                return ParameterValue.constant(new Inventory(node, side, intSide));
            }
            case PAR_ITEM: {
                String itemReg = object.get("item").getAsString();
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemReg));
                int amount = object.has("amount") ? object.get("amount").getAsInt() : 1;
                // @todo 1.15 meta
//                int meta = object.get("meta").getAsInt();
                ItemStack stack = new ItemStack(item, amount);
                if (object.has("nbt")) {
                    String nbt = object.get("nbt").getAsString();
                    CompoundTag tagCompound = null;
                    try {
                        tagCompound = TagParser.parseTag(nbt);
                    } catch (CommandSyntaxException e) {
                        // @todo What to do?
                    }
                    stack.setTag(tagCompound);
                }
                return ParameterValue.constant(stack);
            }
            case PAR_FLUID: {
                String fluidName = object.get("fluid").getAsString();
                int amount = object.get("amount").getAsInt();
                FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)), amount);
                if (object.has("nbt")) {
                    String nbt = object.get("nbt").getAsString();
                    try {
                        fluidStack.setTag(TagParser.parseTag(nbt));
                    } catch (CommandSyntaxException e) {
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
            case PAR_VECTOR:
                JsonArray array = object.get("vector").getAsJsonArray();
                List<Parameter> vector = new ArrayList<>();
                for (JsonElement element : array) {
                    JsonObject job = element.getAsJsonObject();
                    ParameterType t = ParameterType.getByName(job.get("type").getAsString());
                    vector.add(Parameter.builder().type(t).value(readFromJson(t, job)).build());
                }
                return ParameterValue.constant(Collections.unmodifiableList(vector));
        }
        return null;
    }

}
