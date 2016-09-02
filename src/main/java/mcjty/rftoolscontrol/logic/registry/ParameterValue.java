package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.logic.Parameter;

import java.util.List;

public class ParameterValue {

    private final int variableIndex;
    private final Object value;
    private final Function function;
    private final List<Parameter> functionParameters;

    private ParameterValue(int variableIndex, Object value, Function function,
                           List<Parameter> functionParameters) {
        this.variableIndex = variableIndex;
        this.value = value;
        this.function = function;
        this.functionParameters = functionParameters;
    }

    public int getVariableIndex() {
        return variableIndex;
    }

    public Object getValue() {
        return value;
    }

    public Function getFunction() {
        return function;
    }

    public List<Parameter> getFunctionParameters() {
        return functionParameters;
    }

    public boolean isConstant() {
        return variableIndex == -1 && function == null;
    }

    public boolean isVariable() {
        return variableIndex != -1;
    }

    public boolean isFunction() {
        return function != null;
    }

    public static ParameterValue constant(Object value) {
        return new ParameterValue(-1, value, null, null);
    }

    public static ParameterValue variable(int index) {
        return new ParameterValue(index, null, null, null);
    }

    public static ParameterValue function(Function function, List<Parameter> functionParameters) {
        return new ParameterValue(-1, null, function, functionParameters);
    }
}
