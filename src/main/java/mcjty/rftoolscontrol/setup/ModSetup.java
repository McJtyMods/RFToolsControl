package mcjty.rftoolscontrol.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbase.api.control.registry.IFunctionRegistry;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.compat.rftoolssupport.RFToolsSupport;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModSetup extends DefaultModSetup {

    public ModSetup() {
        createTab("rftoolscontrol", () -> new ItemStack(ProcessorModule.PROCESSOR.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });

        RFToolsCtrlMessages.registerMessages("rftoolsctrl");

        Opcodes.init();
        Functions.init();
    }

    public void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("getOpcodeRegistry".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<IOpcodeRegistry, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(new OpcodeRegistry());
            } else if ("getFunctionRegistry".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<IFunctionRegistry, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(new FunctionRegistry());
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
