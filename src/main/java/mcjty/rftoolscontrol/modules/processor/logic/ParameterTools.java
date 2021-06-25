package mcjty.rftoolscontrol.modules.processor.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsbase.api.control.parameters.*;
import mcjty.rftoolscontrol.modules.processor.logic.registry.InventoryUtil;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterTools {

    public static Parameter readFromBuf(PacketBuffer buf) {
        byte b = buf.readByte();
        if (b == -1) {
            return null;
        }
        ParameterType type = ParameterType.values()[b];
        Parameter.Builder builder = Parameter.builder().type(type);
        if (buf.readBoolean()) {
            switch (type) {
                case PAR_STRING:
                    builder.value(ParameterValue.constant(buf.readUtf(32767)));
                    break;
                case PAR_INTEGER:
                    builder.value(ParameterValue.constant(buf.readInt()));
                    break;
                case PAR_LONG:
                    builder.value(ParameterValue.constant(buf.readLong()));
                    break;
                case PAR_FLOAT:
                    builder.value(ParameterValue.constant(buf.readFloat()));
                    break;
                case PAR_NUMBER: {
                    byte t = buf.readByte();
                    switch (t) {
                        case 0:
                            builder.value(ParameterValue.constant(buf.readInt()));
                            break;
                        case 1:
                            builder.value(ParameterValue.constant(buf.readLong()));
                            break;
                        case 2:
                            builder.value(ParameterValue.constant(buf.readFloat()));
                            break;
                        case 3:
                            builder.value(ParameterValue.constant(buf.readDouble()));
                            break;
                    }
                    break;
                }
                case PAR_SIDE: {
                    String nodeName = buf.readUtf(32767);
                    int sideIdx = buf.readByte();
                    Direction side = sideIdx == -1 ? null : Direction.values()[sideIdx];
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
                    builder.value(ParameterValue.constant(ExceptionType.getExceptionForCode(buf.readUtf(32767))));
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

    public static void writeToBuf(PacketBuffer buf, Parameter parameter) {
        buf.writeByte(parameter.getParameterType().ordinal());
        Object value = parameter.getParameterValue().getValue();
        if (value == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        switch (parameter.getParameterType()) {
            case PAR_STRING:
                buf.writeUtf((String) value);
                break;
            case PAR_INTEGER:
                buf.writeInt((Integer) value);
                break;
            case PAR_LONG:
                buf.writeLong((Long) value);
                break;
            case PAR_FLOAT:
                buf.writeFloat((Float) value);
                break;
            case PAR_NUMBER: {
                if (value instanceof Integer) {
                    buf.writeByte(0);
                    buf.writeInt((Integer) value);
                } else if (value instanceof Long) {
                    buf.writeByte(1);
                    buf.writeLong((Long) value);
                } else if (value instanceof Float) {
                    buf.writeByte(2);
                    buf.writeFloat((Float) value);
                } else if (value instanceof Double) {
                    buf.writeByte(3);
                    buf.writeDouble((Double) value);
                }
                break;
            }
            case PAR_SIDE:
                BlockSide bs = (BlockSide) value;
                buf.writeUtf(bs.getNodeNameSafe());
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
                buf.writeUtf(((ExceptionType)value).getCode());
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

    public static CompoundNBT writeToNBT(Parameter parameter) {
        ParameterType type = parameter.getParameterType();
        ParameterValue value = parameter.getParameterValue();
        CompoundNBT parTag = new CompoundNBT();
        parTag.putInt("type", type.ordinal());
        ParameterTypeTools.writeToNBT(parTag, type, value);
        return parTag;
    }

    public static Parameter readFromNBT(CompoundNBT parTag) {
        ParameterType type = ParameterType.values()[parTag.getInt("type")];
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

    /**
     * Warning! Only use this function when the type of the two elements is the same!
     */
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
            case PAR_LONG:
                return ((Long)v1).compareTo((Long)v2);
            case PAR_FLOAT:
                return ((Float)v1).compareTo((Float)v2);
            case PAR_NUMBER:
                return compareNumbers(v1, v2);
            case PAR_SIDE:
                return 0;
            case PAR_BOOLEAN:
                return ((Boolean)v1).compareTo((Boolean)v2);
            case PAR_INVENTORY:
                return 0;
            case PAR_ITEM:
                return Integer.compare(((ItemStack) v1).getCount(), ((ItemStack) v2).getCount());
            case PAR_FLUID:
                return Integer.compare(((FluidStack) v1).getAmount(), ((FluidStack) v2).getAmount());
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

    public static int compareNumbers(Object n1, Object n2) {
        if (n2 == null) {
            n2 = 0;
        }
        if (n1 instanceof Integer) {
            return ((Integer) n1).compareTo(TypeConverters.castToInt(n2));
        }
        if (n1 instanceof Long) {
            return ((Long) n1).compareTo(TypeConverters.castToLong(n2));
        }
        if (n1 instanceof Float) {
            return ((Float) n1).compareTo(TypeConverters.castToFloat(n2));
        }
        if (n1 instanceof Double) {
            return ((Double) n1).compareTo(TypeConverters.castToDouble(n2));
        }
        if (n1 == null) {
            return Integer.compare(0, TypeConverters.castToInt(n2));
        }
        throw new IllegalArgumentException("Can't happen!");
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
                    if (cmp < 0) {
                        maxidx = i;
                        max = vector.get(i);
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
                    if (cmp > 0) {
                        minidx = i;
                        min = vector.get(i);
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

    public static int getLength(@Nullable IParameter parameter) {
        if (parameter == null) {
            return 0;
        }
        ParameterValue value = parameter.getParameterValue();
        if (value == null) {
            return 0;
        }
        Object v = value.getValue();
        switch (parameter.getParameterType()) {
            case PAR_STRING:
                return ((String) v).length();
            case PAR_INTEGER:
                return 0;
            case PAR_LONG:
                return 0;
            case PAR_FLOAT:
                return 0;
            case PAR_NUMBER:
                return 0;
            case PAR_BOOLEAN:
                return 0;
            case PAR_TUPLE:
                return 2;
            case PAR_VECTOR:
                return ((List<?>)v).size();
            case PAR_ITEM:
                return ((ItemStack) v).getCount();
            case PAR_SIDE:
            case PAR_INVENTORY:
            case PAR_FLUID:
            case PAR_EXCEPTION:
                return 0;
        }
        return 0;
    }

    public static Number addNumbers(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return TypeConverters.castToDouble(n1) + TypeConverters.castToDouble(n2);
        } else if (n1 instanceof Float || n2 instanceof Float) {
            return TypeConverters.castToFloat(n1) + TypeConverters.castToFloat(n2);
        } else if (n1 instanceof Long || n2 instanceof Long) {
            return TypeConverters.castToLong(n1) + TypeConverters.castToLong(n2);
        } else {
            return TypeConverters.castToInt(n1) + TypeConverters.castToInt(n2);
        }
    }

    public static Number subtractNumbers(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return TypeConverters.castToDouble(n1) - TypeConverters.castToDouble(n2);
        } else if (n1 instanceof Float || n2 instanceof Float) {
            return TypeConverters.castToFloat(n1) - TypeConverters.castToFloat(n2);
        } else if (n1 instanceof Long || n2 instanceof Long) {
            return TypeConverters.castToLong(n1) - TypeConverters.castToLong(n2);
        } else {
            return TypeConverters.castToInt(n1) - TypeConverters.castToInt(n2);
        }
    }

    public static Number multiplyNumbers(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return TypeConverters.castToDouble(n1) * TypeConverters.castToDouble(n2);
        } else if (n1 instanceof Float || n2 instanceof Float) {
            return TypeConverters.castToFloat(n1) * TypeConverters.castToFloat(n2);
        } else if (n1 instanceof Long || n2 instanceof Long) {
            return TypeConverters.castToLong(n1) * TypeConverters.castToLong(n2);
        } else {
            return TypeConverters.castToInt(n1) * TypeConverters.castToInt(n2);
        }
    }

    public static Number divideNumbers(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return TypeConverters.castToDouble(n1) / TypeConverters.castToDouble(n2);
        } else if (n1 instanceof Float || n2 instanceof Float) {
            return TypeConverters.castToFloat(n1) / TypeConverters.castToFloat(n2);
        } else if (n1 instanceof Long || n2 instanceof Long) {
            return TypeConverters.castToLong(n1) / TypeConverters.castToLong(n2);
        } else {
            return TypeConverters.castToInt(n1) / TypeConverters.castToInt(n2);
        }
    }

    public static Number moduloNumbers(Number n1, Number n2) {
        if (n1 instanceof Double || n2 instanceof Double) {
            return TypeConverters.castToDouble(n1) % TypeConverters.castToDouble(n2);
        } else if (n1 instanceof Float || n2 instanceof Float) {
            return TypeConverters.castToFloat(n1) % TypeConverters.castToFloat(n2);
        } else if (n1 instanceof Long || n2 instanceof Long) {
            return TypeConverters.castToLong(n1) % TypeConverters.castToLong(n2);
        } else {
            return TypeConverters.castToInt(n1) % TypeConverters.castToInt(n2);
        }
    }
}
