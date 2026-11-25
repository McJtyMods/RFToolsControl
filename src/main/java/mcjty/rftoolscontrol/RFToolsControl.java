package mcjty.rftoolscontrol;

import java.util.function.Function;
import mcjty.lib.modules.Modules;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.rftoolscontrol.setup.ModSetup;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import mcjty.rftoolscontrol.setup.Registration;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Item;

public class RFToolsControl implements ModInitializer {

    public static final String MODID = "rftoolscontrol";
    public static final ModSetup setup = new ModSetup();
    public static final Modules modules = new Modules();

    @Override
    public void onInitialize() {
        modules.register(new CraftingStationModule());
        modules.register(new MultiTankModule());
        modules.register(new ProcessorModule());
        modules.register(new ProgrammerModule());
        modules.register(new VariousModule());
        Config.register(modules);
        Registration.register();
        RFToolsCtrlMessages.registerMessages();
        setup.init();
        modules.init();
        setup.registerBlockCapabilities(Registration.RBLOCKS);
        WorkbenchTileEntity.registerCapabilities();
    }

    public static <T extends Item> Function<Item.Properties, T> tab(Function<Item.Properties, T> factory) {
        return properties -> setup.tab(() -> factory.apply(properties)).get();
    }
}
