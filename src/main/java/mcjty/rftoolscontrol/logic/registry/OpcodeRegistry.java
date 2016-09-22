package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.code.Opcode;
import mcjty.rftoolscontrol.api.registry.IOpcodeRegistry;

public class OpcodeRegistry implements IOpcodeRegistry {
    @Override
    public void register(Opcode opcode) {
        Opcodes.register(opcode);
    }
}
