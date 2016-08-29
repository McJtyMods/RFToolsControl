package mcjty.rftoolscontrol.logic.registry;

public class ParameterValue {

    private final int variableIndex;
    private final Object value;

    private ParameterValue(int variableIndex, Object value) {
        this.variableIndex = variableIndex;
        this.value = value;
    }

    public int getVariableIndex() {
        return variableIndex;
    }

    public Object getValue() {
        return value;
    }

    public boolean isConstant() {
        return variableIndex == -1;
    }

    public boolean isVariable() {
        return variableIndex != -1;
    }

    public static ParameterValue constant(Object value) {
        return new ParameterValue(-1, value);
    }

    public static ParameterValue variable(int index) {
        return new ParameterValue(index, null);
    }
}
