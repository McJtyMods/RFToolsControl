package mcjty.rftoolscontrol.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.logic.registry.InventoryUtil;
import mcjty.rftoolscontrol.logic.registry.ParameterTypeTools;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterTools {

    public static Parameter readFromBuf(ByteBuf buf) {
        byte b = buf.readByte();
        if (b == -1) {
            return null;
        }
        ParameterType type = ParameterType.values()[b];
        Parameter.Builder builder = Parameter.builder().type(type);
        if (buf.readBoolean()) {
            switch (type) {
                case PAR_STRING:
                    builder.value(ParameterValue.constant(NetworkTools.readString(buf)));
                    break;
                case PAR_INTEGER:
                    builder.value(ParameterValue.constant(buf.readInt()));
                    break;
                case PAR_FLOAT:
                    builder.value(ParameterValue.constant(buf.readFloat()));
                    break;
                case PAR_SIDE: {
                    String nodeName = NetworkTools.readString(buf);
                    int sideIdx = buf.readByte();
                    EnumFacing side = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
                    builder.value(ParameterValue.constant(new BlockSide(nodeName, side)));
                    break;
                }
                case PAR_BOOLEAN:
                    builder.value(ParameterValue.constant(buf.readBoolean()));
                    break;
                case PAR_INVENTORY:
                    builder.value(ParameterValue.constant(InventoryUtil.readBuf(buf)));
                    break;
                case PAR_ITEM:
                    builder.value(ParameterValue.constant(NetworkTools.readItemStack(buf)));
                    break;
                case PAR_FLUID:
                    builder.value(ParameterValue.constant(NetworkTools.readFluidStack(buf)));
                    break;
                case PAR_EXCEPTION:
                    builder.value(ParameterValue.constant(ExceptionType.getExceptionForCode(NetworkTools.readString(buf))));
                    break;
                case PAR_TUPLE:
                    builder.value(ParameterValue.constant(new Tuple(buf.readInt(), buf.readInt())));
                    break;
                case PAR_VECTOR: {
                    int size = buf.readInt();
                    List<Parameter> vector = new ArrayList<>(size);
                    for (int i = 0 ; i < size ; i++) {
                        vector.add(readFromBuf(buf));
                    }
                    builder.value(ParameterValue.constant(Collections.unmodifiableList(vector)));
                    break;
                }
            }
        }
        return builder.build();
    }

    public static void writeToBuf(ByteBuf buf, Parameter parameter) {
        buf.writeByte(parameter.getParameterType().ordinal());
        Object value = parameter.getParameterValue().getValue();
        if (value == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        switch (parameter.getParameterType()) {
            case PAR_STRING:
                NetworkTools.writeString(buf, (String) value);
                break;
            case PAR_INTEGER:
                buf.writeInt((Integer) value);
                break;
            case PAR_FLOAT:
                buf.writeFloat((Float) value);
                break;
            case PAR_SIDE:
                BlockSide bs = (BlockSide) value;
                NetworkTools.writeString(buf, bs.getNodeName());
                buf.writeByte(bs.getSide() == null ? -1 : bs.getSide().ordinal());
                break;
            case PAR_BOOLEAN:
                buf.writeBoolean((Boolean) value);
                break;
            case PAR_INVENTORY:
                Inventory inv = (Inventory) value;
                InventoryUtil.writeBuf(inv, buf);
                break;
            case PAR_ITEM:
                NetworkTools.writeItemStack(buf, (ItemStack) value);
                break;
            case PAR_FLUID:
                NetworkTools.writeFluidStack(buf, (FluidStack) value);
                break;
            case PAR_EXCEPTION:
                NetworkTools.writeString(buf, ((ExceptionType)value).getCode());
                break;
            case PAR_TUPLE:
                Tuple tuple = (Tuple) value;
                buf.writeInt(tuple.getX());
                buf.writeInt(tuple.getY());
                break;
            case PAR_VECTOR:
                List<Parameter> vector = (List<Parameter>) value;
                buf.writeInt(vector.size());
                for (Parameter p : vector) {
                    writeToBuf(buf, p);
                }
                break;
        }
    }

    public static NBTTagCompound writeToNBT(Parameter parameter) {
        ParameterType type = parameter.getParameterType();
        ParameterValue value = parameter.getParameterValue();
        NBTTagCompound parTag = new NBTTagCompound();
        parTag.setInteger("type", type.ordinal());
        ParameterTypeTools.writeToNBT(parTag, type, value);
        return parTag;
    }

    public static Parameter readFromNBT(NBTTagCompound parTag) {
        ParameterType type = ParameterType.values()[parTag.getInteger("type")];
        ParameterValue value = ParameterTypeTools.readFromNBT(parTag, type);
        return Parameter.builder().type(type).value(value).build();
    }

    public static Parameter readFromJson(JsonObject object) {
        ParameterType type = ParameterType.getByName(object.get("type").getAsString());
        ParameterValue value = ParameterTypeTools.readFromJson(type, object.get("value").getAsJsonObject());
        return Parameter.builder().type(type).value(value).build();
    }

    public static JsonElement getJsonElement(Parameter parameter) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("type", new JsonPrimitive(parameter.getParameterType().getName()));
        jsonObject.add("value", ParameterTypeTools.writeToJson(parameter.getParameterType(), parameter.getParameterValue()));
        return jsonObject;
    }

    public static int compare(Parameter par1, Parameter par2) {
        Object v1 = par1.getParameterValue().getValue();
        Object v2 = par2.getParameterValue().getValue();
        if (v1 == null) {
            return v2 == null ? 0 : 1;
        }
        if (v2 == null) {
            return -1;
        }

        switch (par2.getParameterType()) {
            case PAR_STRING:
                return ((String)v1).compareTo((String)v2);
            case PAR_INTEGER:
                return ((Integer)v1).compareTo((Integer)v2);
            case PAR_FLOAT:
                return ((Float)v1).compareTo((Float)v2);
            case PAR_SIDE:
                return 0;
            case PAR_BOOLEAN:
                return ((Boolean)v1).compareTo((Boolean)v2);
            case PAR_INVENTORY:
                return 0;
            case PAR_ITEM:
                return Integer.compare(ItemStackTools.getStackSize((ItemStack) v1), ItemStackTools.getStackSize((ItemStack) v2));
            case PAR_FLUID:
                return Integer.compare(((FluidStack) v1).amount, ((FluidStack) v2).amount);
            case PAR_EXCEPTION:
                return 0;
            case PAR_TUPLE: {
                Tuple t1 = (Tuple) v1;
                Tuple t2 = (Tuple) v2;
                if (t1.getX() == t2.getX()) {
                    return Integer.compare(t1.getY(), t2.getY());
                }
                return Integer.compare(t1.getX(), t2.getX());
            }
            case PAR_VECTOR: {
                List<Parameter> t1 = (List<Parameter>) v1;
                List<Parameter> t2 = (List<Parameter>) v2;
                if (t1.size() == t2.size()) {
                    for (int i = 0 ; i < t1.size() ; i++) {
                        Parameter p1 = t1.get(i);
                        Parameter p2 = t2.get(i);
                        int rc = compare(p1, p2);
                        if (rc != 0) {
                            return rc;
                        }
                    }
                    return 0;
                } else {
                    return Integer.compare(t1.size(), t2.size());
                }
            }
        }
        return 0;
    }

    public static int getMaxidxVector(List<Parameter> vector) {
        int maxidx = -1;
        Parameter max = null;
        for (int i = 0 ; i < vector.size() ; i++) {
            if (vector.get(i) != null) {
                if (max == null) {
                    max = vector.get(i);
                    maxidx = i;
                } else {
                    int cmp = compare(max, vector.get(i));
                    if (cmp > 0) {
                        maxidx = i;
                    }
                }
            }
        }
        return maxidx;
    }

    public static int getMinidxVector(List<Parameter> vector) {
        int minidx = -1;
        Parameter min = null;
        for (int i = 0 ; i < vector.size() ; i++) {
            if (vector.get(i) != null) {
                if (min == null) {
                    min = vector.get(i);
                    minidx = i;
                } else {
                    int cmp = compare(min, vector.get(i));
                    if (cmp < 0) {
                        minidx = i;
                    }
                }
            }
        }
        return minidx;
    }

    public static int getSumVector(List<Parameter> vector) {
        int sum = 0;
        for (Parameter el : vector) {
            if (el != null) {
                sum += TypeConverters.convertToInt(el);
            }
        }
        return sum;
    }

}
