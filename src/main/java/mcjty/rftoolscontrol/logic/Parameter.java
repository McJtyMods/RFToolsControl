package mcjty.rftoolscontrol.logic;

import mcjty.rftoolscontrol.logic.registry.ParameterDescription;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;

public class Parameter {

    private final ParameterDescription parameterDescription;
    private final ParameterValue parameterValue;

    private Parameter(Builder builder) {
        parameterDescription = builder.parameterDescription;
        parameterValue = builder.parameterValue;
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
