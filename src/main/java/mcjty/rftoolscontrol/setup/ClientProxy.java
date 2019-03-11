package mcjty.rftoolscontrol.setup;

import mcjty.lib.setup.DefaultClientProxy;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.commands.ProgramCommand;
import mcjty.rftoolscontrol.logic.editors.ParameterEditors;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(RFToolsControl.MODID);

        ParameterEditors.init();
        ClientCommandHandler.instance.registerCommand(new ProgramCommand());
    }
}
