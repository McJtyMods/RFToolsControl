package mcjty.rftoolscontrol.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;

public class ModSetup extends DefaultModSetup {

    @Override
    public void init() {
        super.init();
        CommandHandler.registerCommands();
        Opcodes.init();
        Functions.init();
    }

    @Override
    protected void setupModCompat() {
    }
}
