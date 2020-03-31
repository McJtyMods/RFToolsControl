package mcjty.rftoolscontrol.modules.processor.logic.compiled;

import mcjty.rftoolsbase.api.control.code.ICompiledOpcode;
import mcjty.rftoolsbase.api.control.code.IOpcodeRunnable;
import mcjty.rftoolsbase.api.control.code.Opcode;
import mcjty.rftoolsbase.api.control.parameters.IParameter;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import mcjty.rftoolscontrol.modules.processor.logic.running.RunningProgram;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CompiledOpcode implements ICompiledOpcode {

    private final Opcode opcode;
    private final List<IParameter> parameters;
    private final int primaryIndex;
    private final int secondaryIndex;
    private final int gridX;
    private final int gridY;

    private CompiledOpcode(Builder builder) {
        opcode = builder.opcode;
        parameters = new ArrayList<>(builder.parameters);
        primaryIndex = builder.primaryIndex;
        secondaryIndex = builder.secondaryIndex;
        gridX = builder.gridX;
        gridY = builder.gridY;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    @Override
    @Nonnull
    public List<IParameter> getParameters() {
        return parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getPrimaryIndex() {
        return primaryIndex;
    }

    public int getSecondaryIndex() {
        return secondaryIndex;
    }

    public IOpcodeRunnable.OpcodeResult run(ProcessorTileEntity processor, RunningProgram program) throws ProgException {
        return opcode.getRunnable().run(processor, program, this);
    }

    public static class Builder {

        private Opcode opcode;
        private final List<Parameter> parameters = new ArrayList<>();
        private int primaryIndex;
        private int secondaryIndex;
        private int gridX;
        private int gridY;

        public Builder opcode(Opcode opcode) {
            this.opcode = opcode;
            return this;
        }

        public Builder grid(int x, int y) {
            gridX = x;
            gridY = y;
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
