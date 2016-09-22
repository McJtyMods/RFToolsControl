package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.code.Function;
import mcjty.rftoolscontrol.api.registry.IFunctionRegistry;

public class FunctionRegistry implements IFunctionRegistry {
    @Override
    public void register(Function function) {
        Functions.register(function);
    }
}
