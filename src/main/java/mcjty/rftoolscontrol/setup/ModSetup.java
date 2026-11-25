package mcjty.rftoolscontrol.setup;

import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbase.api.control.registry.IFunctionRegistry;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.compat.rftoolssupport.RFToolsSupport;
import mcjty.rftoolscontrol.modules.processor.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });

        Opcodes.init();
        Functions.init();
    }

    @SuppressWarnings("unchecked")
    public void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("getOpcodeRegistry".equalsIgnoreCase(message.method())) {
                Supplier<?> supplier = message.messageSupplier();
                ((Function<IOpcodeRegistry, Void>) supplier.get()).apply(new OpcodeRegistry());
            } else if ("getFunctionRegistry".equalsIgnoreCase(message.method())) {
                Supplier<?> supplier = message.messageSupplier();
                ((Function<IFunctionRegistry, Void>) supplier.get()).apply(new FunctionRegistry());
            }
        });
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        InterModComms.sendTo("rftoolsutility", "getScreenModuleRegistry", RFToolsSupport.GetScreenModuleRegistry::new);
    }
}
