package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.running.RunningProgram;

public interface OpcodeRunnable {
    // Return true to process to primary output, else to secondary output
    boolean run(ProcessorTileEntity processor, RunningProgram program);
}
