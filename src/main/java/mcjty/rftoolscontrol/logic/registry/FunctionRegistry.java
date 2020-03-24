package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolsbase.api.control.code.Function;
import mcjty.rftoolsbase.api.control.registry.IFunctionRegistry;

public class FunctionRegistry implements IFunctionRegistry {
    @Override
    public void register(Function function) {
        Functions.register(function);
    }
}
