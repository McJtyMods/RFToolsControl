package mcjty.rftoolscontrol.api.code;

import mcjty.rftoolscontrol.api.machines.IProcessor;
import mcjty.rftoolscontrol.api.machines.IProgram;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;

public interface IFunctionRunnable {
    Object run(IProcessor processor, IProgram program);
}
