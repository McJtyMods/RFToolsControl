package mcjty.rftoolscontrol.modules.processor.logic;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.rftoolsbase.api.control.parameters.IParameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A representation of a parameter.
 */
public class Parameter implements IParameter {

    private final ParameterType parameterType;
    private final ParameterValue parameterValue;

    private Parameter(Builder builder) {
        parameterType = builder.parameterType;
        parameterValue = builder.parameterValue;
    }

    public static class Serializer implements ISerializer<Parameter> {
        @Override
        public Function<FriendlyByteBuf, Parameter> getDeserializer() {
            return ParameterTools::readFromBuf;
        }

        @Override
        public BiConsumer<FriendlyByteBuf, Parameter> getSerializer() {
            return ParameterTools::writeToBuf;
        }
    }

    @Override
    public int compareTo(@Nonnull IParameter parameter) {
        return switch (parameterType) {
            case PAR_STRING -> TypeConverters.convertToString(this).compareTo(TypeConverters.convertToString(parameter));
            case PAR_INTEGER -> Integer.compare(TypeConverters.convertToInt(this), TypeConverters.convertToInt(parameter));
            case PAR_FLOAT -> Float.compare(TypeConverters.convertToFloat(this), TypeConverters.convertToFloat(parameter));
            case PAR_SIDE -> Objects.compare(TypeConverters.convertToSide(this), TypeConverters.convertToSide(parameter),
                    Comparator.naturalOrder());
            case PAR_BOOLEAN -> Boolean.compare(TypeConverters.convertToBool(this), TypeConverters.convertToBool(parameter));
            case PAR_INVENTORY -> 0;       // Undefined
            case PAR_ITEM -> Objects.compare(TypeConverters.convertToItem(this), TypeConverters.convertToItem(parameter),
                    Comparator.comparing(s -> s.getItem().getRegistryName()));
            case PAR_EXCEPTION -> 0;
            case PAR_TUPLE -> Objects.compare(TypeConverters.convertToTuple(this), TypeConverters.convertToTuple(parameter),
                    Comparator.naturalOrder());
            case PAR_FLUID -> Objects.compare(TypeConverters.convertToFluid(this), TypeConverters.convertToFluid(parameter),
                    Comparator.comparing(s -> s.getFluid().getRegistryName().toString()));
            case PAR_VECTOR -> Objects.compare(TypeConverters.convertToVector(this), TypeConverters.convertToVector(parameter),
                    Parameter::compareLists);
            case PAR_LONG -> Long.compare(TypeConverters.convertToLong(this), TypeConverters.convertToLong(parameter));
            case PAR_NUMBER -> Objects.compare(TypeConverters.convertToNumber(this), TypeConverters.convertToNumber(parameter),
                    (n1, n2) -> {
                        Double d1 = n1.doubleValue();
                        Double d2 = n2.doubleValue();
                        return Objects.compare(d1, d2, Comparator.naturalOrder());
                    });
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return parameterType == parameter.parameterType && Objects.equals(parameterValue, parameter.parameterValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterType, parameterValue);
    }

    private static int compareLists(List<Parameter> l1, List<Parameter> l2) {
        int i = 0;
        while (i < l1.size() && i < l2.size()) {
            int rc = Objects.compare(l1.get(i), l2.get(i), Comparator.naturalOrder());
            if (rc != 0) {
                return rc;
            }
            i++;
        }
        if (i < l1.size()) {
            return 1;
        } else if (i < l2.size()) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean isSet() {
        return parameterValue != null && parameterValue.getValue() != null;
    }

    @Override
    public ParameterType getParameterType() {
        return parameterType;
    }

    @Override
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
