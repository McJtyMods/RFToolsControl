package mcjty.rftoolscontrol.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.logic.registry.InventoryUtil;
import mcjty.rftoolscontrol.logic.registry.ParameterTypeTools;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

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
                case PAR_SIDE:
                    String nodeName = NetworkTools.readString(buf);
                    int sideIdx = buf.readByte();
                    EnumFacing side = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
                    builder.value(ParameterValue.constant(new BlockSide(nodeName, side)));
                    break;
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
}
