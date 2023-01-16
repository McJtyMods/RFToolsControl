package mcjty.rftoolscontrol;

import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.rftoolscontrol.compat.RFToolsStuff;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.rftoolscontrol.setup.ModSetup;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

@Mod(RFToolsControl.MODID)
public class RFToolsControl {
    public static final String MODID = "rftoolscontrol";

    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();
    public static RFToolsControl instance;

    public RFToolsControl() {
        instance = this;
        RFToolsStuff.init();
        setupModules();

        Config.register(modules);

        // This has to be done VERY early
//        FluidRegistry.enableUniversalBucket();
        Registration.register();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(setup::init);
        bus.addListener(setup::processIMC);
        bus.addListener(modules::init);
        bus.addListener(this::onDataGen);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            bus.addListener(modules::initClient);
        });
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }

    private void onDataGen(GatherDataEvent event) {
        DataGen datagen = new DataGen(MODID, event);
        modules.datagen(datagen);
        datagen.generate();
    }

    private void setupModules() {
        modules.register(new CraftingStationModule());
        modules.register(new MultiTankModule());
        modules.register(new ProcessorModule());
        modules.register(new ProgrammerModule());
        modules.register(new VariousModule());
    }

}
