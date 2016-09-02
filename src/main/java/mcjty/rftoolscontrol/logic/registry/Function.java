package mcjty.rftoolscontrol.logic.registry;

import java.util.ArrayList;
import java.util.List;

public class Function {

    private final String id;
    private final String name;
    private final List<ParameterDescription> parameters;
    private final FunctionRunnable functionRunnable;
    private final ParameterType returnType;

    private Function(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.functionRunnable = builder.functionRunnable;
        this.parameters = new ArrayList<>(builder.parameters);
        this.returnType = builder.returnType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FunctionRunnable getFunctionRunnable() {
        return functionRunnable;
    }

    public ParameterType getReturnType() {
        return returnType;
    }

    public List<ParameterDescription> getParameters() {
        return parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final FunctionRunnable NOOP = ((processor, program, opcode) -> null);

        private String id;
        private FunctionRunnable functionRunnable = NOOP;
        private ParameterType returnType;
        private String name;
        private List<ParameterDescription> parameters = new ArrayList<>();

        public Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder runnable(FunctionRunnable runnable) {
            this.functionRunnable = runnable;
            return this;
        }

        public Builder type(ParameterType type) {
            this.returnType = type;
            return this;
        }

        public Builder parameter(ParameterDescription parameter) {
            parameters.add(parameter);
            return this;
        }

        public Function build() {
            return new Function(this);
        }
    }
}
