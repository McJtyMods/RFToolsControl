package mcjty.rftoolscontrol;

import java.util.Map;
import java.util.function.Function;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.rftoolsbase.tools.ClientItemDefinitions;
import mcjty.rftoolscontrol.compat.RFToolsStuff;
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
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RFToolsControl.MODID)
public class RFToolsControl {
    public static final String MODID = "rftoolscontrol";

    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();
    public static RFToolsControl instance;

    public RFToolsControl(ModContainer mod, IEventBus bus, Dist dist) {
        instance = this;
        setupModules(bus, dist);
        RFToolsStuff.init();

        Config.register(mod, bus, modules);

        Registration.register(bus);

        bus.addListener(setup::init);
        bus.addListener(setup::processIMC);
        bus.addListener(modules::init);
        bus.addListener(this::onDataGen);
        bus.addListener(RFToolsCtrlMessages::registerMessages);
        bus.addListener(WorkbenchTileEntity::registerCapabilities);
        bus.addListener(setup.getBlockCapabilityRegistrar(Registration.RBLOCKS));

        if (dist.isClient()) {
            bus.addListener(modules::initClient);
        }
    }

    public static <T extends Item> Function<Item.Properties, T> tab(Function<Item.Properties, T> factory) {
        return properties -> setup.tab(() -> factory.apply(properties)).get();
    }

    private void onDataGen(GatherDataEvent.Client event) {
        DataGen datagen = new DataGen(MODID, event);
        modules.datagen(datagen, event.getWorldLookupProvider());
        datagen.generate();
        event.getGenerator().addProvider(true, new ClientItemDefinitions(event.getGenerator().getPackOutput(), MODID, Map.of()));
    }

    private void setupModules(IEventBus bus, Dist dist) {
        modules.register(new CraftingStationModule(bus));
        modules.register(new MultiTankModule(bus));
        modules.register(new ProcessorModule(bus));
        modules.register(new ProgrammerModule(bus));
        modules.register(new VariousModule(bus));
    }

}
