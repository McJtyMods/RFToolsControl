package mcjty.rftoolscontrol.api;

import mcjty.rftoolscontrol.api.paremeters.ParameterValue;

public interface IFunctionRunnable {
    ParameterValue run(IProcessor processor, IProgram program);
}
