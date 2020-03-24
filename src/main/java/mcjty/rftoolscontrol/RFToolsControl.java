package mcjty.rftoolscontrol;

import mcjty.lib.base.ModBase;
import mcjty.rftoolscontrol.items.manual.GuiRFToolsManual;
import mcjty.rftoolscontrol.setup.ModSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsControl.MODID)
public class RFToolsControl implements ModBase {
    public static final String MODID = "rftoolscontrol";

    public static ModSetup setup = new ModSetup();

    public static RFToolsControl instance;

    public RFToolsControl() {
        instance = this;
        // This has to be done VERY early
//        FluidRegistry.enableUniversalBucket();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> setup.initClient(event));
    }

    // @todo 1.15
//    @Mod.EventHandler
//    public void imcCallback(FMLInterModComms.IMCEvent event) {
//        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
//            if (message.key.equalsIgnoreCase("getOpcodeRegistry")) {
//                Optional<Function<IOpcodeRegistry, Void>> value = message.getFunctionValue(IOpcodeRegistry.class, Void.class);
//                value.get().apply(new OpcodeRegistry());
//            } else if (message.key.equalsIgnoreCase("getFunctionRegistry")) {
//                Optional<Function<IFunctionRegistry, Void>> value = message.getFunctionValue(IFunctionRegistry.class, Void.class);
//                value.get().apply(new FunctionRegistry());
//            }
//        }
//
//    }

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
