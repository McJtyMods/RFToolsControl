package mcjty.rftoolscontrol.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public ModSetup() {
        createTab("rftoolscontrol", () -> new ItemStack(ProcessorSetup.PROCESSOR.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        DeferredWorkQueue.runLater(() -> {
            CommandHandler.registerCommands();
        });

        RFToolsCtrlMessages.registerMessages("rftoolsctrl");

        Opcodes.init();
        Functions.init();
    }

    public void initClient(FMLClientSetupEvent e) {
//        OBJLoader.INSTANCE.addDomain(RFToolsControl.MODID);
//
        ParameterEditors.init();
//        ClientCommandHandler.instance.registerCommand(new ProgramCommand());
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
//        FMLInterModComms.sendFunctionMessage("rftools", "getScreenModuleRegistry", "mcjty.rftoolscontrol.compat.rftoolssupport.RFToolsSupport$GetScreenModuleRegistry");
    }
}
