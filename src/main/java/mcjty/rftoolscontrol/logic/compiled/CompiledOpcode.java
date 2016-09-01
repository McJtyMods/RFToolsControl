package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.registry.Opcode;

import java.util.ArrayList;
import java.util.List;

public class CompiledOpcode {

    private final Opcode opcode;
    private final List<Parameter> parameters;
    private final int primaryIndex;
    private final int secondaryIndex;

    private CompiledOpcode(Builder builder) {
        opcode = builder.opcode;
        parameters = new ArrayList<>(builder.parameters);
        primaryIndex = builder.primaryIndex;
        secondaryIndex = builder.secondaryIndex;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPrimaryIndex() {
        return primaryIndex;
    }

    public int getSecondaryIndex() {
        return secondaryIndex;
    }

    public static class Builder {

        private Opcode opcode;
        private List<Parameter> parameters = new ArrayList<>();
        private int primaryIndex;
        private int secondaryIndex;

        public Builder opcode(Opcode opcode) {
            this.opcode = opcode;
            return this;
        }

        public Builder parameter(Parameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public Builder primaryIndex(int primaryIndex) {
            this.primaryIndex = primaryIndex;
            return this;
        }

        public Builder secondaryIndex(int secondaryIndex) {
            this.secondaryIndex = secondaryIndex;
            return this;
        }

        public CompiledOpcode build() {
            return new CompiledOpcode(this);
        }
    }
}
