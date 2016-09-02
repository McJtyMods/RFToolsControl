package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.logic.registry.ParameterDescription;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.nbt.NBTTagCompound;

public class Parameter {

    private final ParameterDescription parameterDescription;
    private final ParameterValue parameterValue;

    private Parameter(Builder builder) {
        parameterDescription = builder.parameterDescription;
        parameterValue = builder.parameterValue;
    }

    public static Parameter readFromNBT(NBTTagCompound parTag) {
        String name = parTag.getString("name");
        ParameterType type = ParameterType.values()[parTag.getInteger("type")];
        ParameterDescription description = ParameterDescription.builder().name(name).type(type).build();
        ParameterValue value = type.readFromNBT(parTag);
        return builder().description(description).value(value).build();
    }

    public static NBTTagCompound writeToNBT(Parameter parameter) {
        String name = parameter.getParameterDescription().getName();
        ParameterType type = parameter.getParameterDescription().getType();
        ParameterValue value = parameter.getParameterValue();
        NBTTagCompound parTag = new NBTTagCompound();
        parTag.setString("name", name);
        parTag.setInteger("type", type.ordinal());
        type.writeToNBT(parTag, value);
        return parTag;
    }

    public ParameterDescription getParameterDescription() {
        return parameterDescription;
    }

    public ParameterValue getParameterValue() {
        return parameterValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ParameterDescription parameterDescription;
        private ParameterValue parameterValue;

        public Builder description(ParameterDescription description) {
            this.parameterDescription = description;
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
