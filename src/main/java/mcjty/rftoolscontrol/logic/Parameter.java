package mcjty.rftoolscontrol.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class Parameter {

    private final ParameterType parameterType;
    private final ParameterValue parameterValue;

    private Parameter(Builder builder) {
        parameterType = builder.parameterType;
        parameterValue = builder.parameterValue;
    }

    public JsonElement getJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("type", new JsonPrimitive(parameterType.getName()));
        jsonObject.add("value", parameterType.writeToJson(parameterValue));
        return jsonObject;
    }

    public static Parameter readFromJson(JsonObject object) {
        ParameterType type = ParameterType.getByName(object.get("type").getAsString());
        ParameterValue value = type.readFromJson(object);
        return builder().type(type).value(value).build();
    }

    public static Parameter readFromNBT(NBTTagCompound parTag) {
        ParameterType type = ParameterType.values()[parTag.getInteger("type")];
        ParameterValue value = type.readFromNBT(parTag);
        return builder().type(type).value(value).build();
    }

    public static NBTTagCompound writeToNBT(Parameter parameter) {
        ParameterType type = parameter.getParameterType();
        ParameterValue value = parameter.getParameterValue();
        NBTTagCompound parTag = new NBTTagCompound();
        parTag.setInteger("type", type.ordinal());
        type.writeToNBT(parTag, value);
        return parTag;
    }

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
                    builder.value(ParameterValue.constant(Inventory.readBuf(buf)));
                    break;
                case PAR_ITEM:
                    builder.value(ParameterValue.constant(NetworkTools.readItemStack(buf)));
                    break;
                case PAR_EXCEPTION:
                    builder.value(ParameterValue.constant(ExceptionType.getExceptionForCode(NetworkTools.readString(buf))));
                    break;
            }
        }
        return builder.build();
    }

    public void writeToBuf(ByteBuf buf) {
        buf.writeByte(getParameterType().ordinal());
        Object value = getParameterValue().getValue();
        if (value == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        switch (getParameterType()) {
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
                inv.writeBuf(buf);
                break;
            case PAR_ITEM:
                NetworkTools.writeItemStack(buf, (ItemStack) value);
                break;
            case PAR_EXCEPTION:
                NetworkTools.writeString(buf, ((ExceptionType)value).getCode());
                break;
        }
    }

    public boolean isSet() {
        return parameterValue != null && parameterValue.getValue() != null;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public ParameterValue getParameterValue() {
        return parameterValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ParameterType parameterType;
        private ParameterValue parameterValue;

        public Builder type(ParameterType parameterType) {
            this.parameterType = parameterType;
            return this;
        }

        public Builder value(ParameterValue value) {
            this.parameterValue = value;
            return this;
        }

        public Parameter build() {
            return new Parameter(this);
        }

    }
}
