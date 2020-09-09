package mcjty.rftoolscontrol;

import mcjty.lib.modules.Modules;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.rftoolscontrol.setup.ModSetup;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsControl.MODID)
public class RFToolsControl {
    public static final String MODID = "rftoolscontrol";

    public static ModSetup setup = new ModSetup();
    private Modules modules = new Modules();
    public static RFToolsControl instance;

    public RFToolsControl() {
        instance = this;
        setupModules();

        Config.register(modules);

        // This has to be done VERY early
//        FluidRegistry.enableUniversalBucket();
        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::init);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::initClient);
        });
    }

    private void setupModules() {
        modules.register(new CraftingStationModule());
        modules.register(new MultiTankModule());
        modules.register(new ProcessorModule());
        modules.register(new ProgrammerModule());
        modules.register(new VariousModule());
    }

}
