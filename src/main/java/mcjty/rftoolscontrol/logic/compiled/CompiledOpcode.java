package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.logic.registry.Opcode;

public class CompiledOpcode {

    private final Opcode opcode;

    public CompiledOpcode(Opcode opcode) {
        this.opcode = opcode;
    }

    public Opcode getOpcode() {
        return opcode;
    }
}
