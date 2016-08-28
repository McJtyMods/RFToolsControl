package mcjty.rftoolscontrol.logic.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Operand {

    private final String id;
    private final OperandOutput operandOutput;
    private final List<Parameter> parameters;

    private final int iconU;
    private final int iconV;

    private Operand(Builder builder) {
        this.id = builder.id;
        this.operandOutput = builder.operandOutput;
        this.parameters = new ArrayList<>(builder.parameters);
        this.iconU = builder.iconU;
        this.iconV = builder.iconV;
    }

    public String getId() {
        return id;
    }

    public OperandOutput getOperandOutput() {
        return operandOutput;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public int getIconU() {
        return iconU;
    }

    public int getIconV() {
        return iconV;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private OperandOutput operandOutput;
        private int iconU;
        private int iconV;
        private List<Parameter> parameters = new ArrayList<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder operandOutput(OperandOutput operandOutput) {
            this.operandOutput = operandOutput;
            return this;
        }

        public Builder icon(int u, int v) {
            this.iconU = u;
            this.iconV = v;
            return this;
        }

        public Builder parameter(Parameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public Operand build() {
            return new Operand(this);
        }
    }
}
