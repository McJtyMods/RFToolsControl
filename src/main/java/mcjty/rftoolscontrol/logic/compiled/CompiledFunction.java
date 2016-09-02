package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.registry.Function;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import mcjty.rftoolscontrol.logic.running.RunningProgram;

import java.util.ArrayList;
import java.util.List;

public class CompiledFunction {

    private final Function function;
    private final List<Parameter> parameters;

    private CompiledFunction(Builder builder) {
        function = builder.function;
        parameters = new ArrayList<>(builder.parameters);
    }

    public Function getFunction() {
        return function;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ParameterValue run(ProcessorTileEntity processor, RunningProgram program) {
        return function.getFunctionRunnable().run(processor, program, this);
    }

    public static class Builder {

        private Function function;
        private List<Parameter> parameters = new ArrayList<>();

        public Builder opcode(Function opcode) {
            this.function = opcode;
            return this;
        }

        public Builder parameter(Parameter parameter) {
            parameters.add(parameter);
            return this;
        }

        public CompiledFunction build() {
            return new CompiledFunction(this);
        }
    }
}
