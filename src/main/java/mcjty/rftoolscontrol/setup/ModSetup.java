package mcjty.rftoolscontrol.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Function;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init() {
        super.init();
        CommandHandler.registerCommands();
        Opcodes.init();
        registerExternalOpcodes();
        Functions.init();
    }

    @SuppressWarnings("unchecked")
    private static void registerExternalOpcodes() {
        IOpcodeRegistry registry = new OpcodeRegistry();
        FabricLoader.getInstance().invokeEntrypoints("rftoolscontrol-opcodes", Function.class,
                entrypoint -> ((Function<IOpcodeRegistry, Void>) entrypoint).apply(registry));
    }

    @Override
    protected void setupModCompat() {
    }
}
