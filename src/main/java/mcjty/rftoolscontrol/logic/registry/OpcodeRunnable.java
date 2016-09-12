package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.running.ProgException;
import mcjty.rftoolscontrol.logic.running.RunningProgram;

public interface OpcodeRunnable {
    enum OpcodeResult {
        POSITIVE,       // Go to positive end
        NEGATIVE,       // Go to negative end
        HOLD            // Stay at this opcode
    }


    // Return true to process to primary output, else to secondary output
    OpcodeResult run(ProcessorTileEntity processor, RunningProgram program, CompiledOpcode opcode);
}
