package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.IProcessor;
import mcjty.rftoolscontrol.api.IProgram;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.running.RunningProgram;

public interface FunctionRunnable {
    ParameterValue run(IProcessor processor, IProgram program, Function function);
}
