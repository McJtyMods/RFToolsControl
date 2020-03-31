package mcjty.rftoolscontrol;

import mcjty.lib.base.ModBase;
import mcjty.rftoolsbase.api.control.registry.IFunctionRegistry;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.setup.ConfigSetup;
import mcjty.rftoolscontrol.modules.processor.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.setup.ModSetup;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Function;
import java.util.function.Supplier;

@Mod(RFToolsControl.MODID)
public class RFToolsControl implements ModBase {
    public static final String MODID = "rftoolscontrol";

    public static ModSetup setup = new ModSetup();

    public static RFToolsControl instance;

    public RFToolsControl() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSetup.COMMON_CONFIG);

        // This has to be done VERY early
//        FluidRegistry.enableUniversalBucket();
        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> setup.initClient(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
    }

    private void processIMC(final InterModProcessEvent event) {
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
    public String getModId() {
        return MODID;
    }

    @Override
    public void openManual(PlayerEntity player, int bookIndex, String page) {
//        GuiRFToolsManual.locatePage = page;
//        player.openGui(RFToolsControl.instance, bookIndex, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    // @todo move me!
    public static final String SHIFT_MESSAGE = "<Press Shift>";
}
