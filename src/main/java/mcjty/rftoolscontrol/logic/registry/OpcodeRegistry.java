package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolsbase.api.control.code.Opcode;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;

public class OpcodeRegistry implements IOpcodeRegistry {
    @Override
    public void register(Opcode opcode) {
        Opcodes.register(opcode);
    }
}
