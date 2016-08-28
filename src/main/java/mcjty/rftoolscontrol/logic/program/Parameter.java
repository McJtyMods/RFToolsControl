package mcjty.rftoolscontrol.logic.program;

public class Parameter {

    private final String name;
    private final ParameterType type;

    private Parameter(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public ParameterType getType() {
        return type;
    }

    public static class Builder {

        private String name;
        private ParameterType type;

        public Builder type(ParameterType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Parameter build() {
            return new Parameter(this);
        }
    }
}
