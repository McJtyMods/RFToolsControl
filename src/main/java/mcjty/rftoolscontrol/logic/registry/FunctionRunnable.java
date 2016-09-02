package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.running.RunningProgram;

public interface FunctionRunnable {
    ParameterValue run(ProcessorTileEntity processor, RunningProgram program, Function function);
}
