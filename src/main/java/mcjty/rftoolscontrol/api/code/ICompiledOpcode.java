package mcjty.rftoolscontrol.api.code;

import mcjty.rftoolscontrol.logic.Parameter;

import java.util.List;

/**
 * Representation of a compiled opcode
 */
public interface ICompiledOpcode {

    List<Parameter> getParameters();
}
