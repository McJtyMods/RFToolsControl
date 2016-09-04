package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.nbt.NBTTagCompound;

public class Parameter {

    private final ParameterType parameterType;
    private final ParameterValue parameterValue;

    private Parameter(Builder builder) {
        parameterType = builder.parameterType;
        parameterValue = builder.parameterValue;
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
