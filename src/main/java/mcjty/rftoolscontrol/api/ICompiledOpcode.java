package mcjty.rftoolscontrol.api;

import mcjty.rftoolscontrol.logic.Parameter;

import java.util.List;

/**
 * Representation of a compiled opcode
 */
public interface ICompiledOpcode {

    List<Parameter> getParameters();
}
