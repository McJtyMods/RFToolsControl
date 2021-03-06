package mcjty.rftoolscontrol;

import mcjty.lib.base.ModBase;
import mcjty.lib.proxy.IProxy;
import mcjty.rftoolscontrol.api.registry.IFunctionRegistry;
import mcjty.rftoolscontrol.api.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.items.manual.GuiRFToolsManual;
import mcjty.rftoolscontrol.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.setup.ModSetup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

import java.util.Optional;
import java.util.function.Function;

@Mod(modid = RFToolsControl.MODID, name="RFTools Control",
        dependencies =
                        "required-after:mcjtylib_ng@[" + RFToolsControl.MIN_MCJTYLIB_VER + ",);" +
                        "required-after:rftools@[" + RFToolsControl.MIN_RFTOOLS_VER + ",);" +
                        "after:forge@[" + RFToolsControl.MIN_FORGE11_VER + ",)",
        acceptedMinecraftVersions = "[1.12,1.13)",
        version = RFToolsControl.VERSION)
public class RFToolsControl implements ModBase {
    public static final String MODID = "rftoolscontrol";
    public static final String VERSION = "2.0.2";
    public static final String MIN_RFTOOLS_VER = "7.70";
    public static final String MIN_FORGE11_VER = "13.19.0.2176";
    public static final String MIN_MCJTYLIB_VER = "3.5.0";

    @SidedProxy(clientSide="mcjty.rftoolscontrol.setup.ClientProxy", serverSide="mcjty.rftoolscontrol.setup.ServerProxy")
    public static IProxy proxy;
    public static ModSetup setup = new ModSetup();

    @Mod.Instance(MODID)
    public static RFToolsControl instance;

    public RFToolsControl() {
        // This has to be done VERY early
        FluidRegistry.enableUniversalBucket();
    }

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        setup.preInit(e);
        proxy.preInit(e);
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandRftDim());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
    }

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if (message.key.equalsIgnoreCase("getOpcodeRegistry")) {
                Optional<Function<IOpcodeRegistry, Void>> value = message.getFunctionValue(IOpcodeRegistry.class, Void.class);
                value.get().apply(new OpcodeRegistry());
            } else if (message.key.equalsIgnoreCase("getFunctionRegistry")) {
                Optional<Function<IFunctionRegistry, Void>> value = message.getFunctionValue(IFunctionRegistry.class, Void.class);
                value.get().apply(new FunctionRegistry());
            }
        }

    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void openManual(EntityPlayer player, int bookIndex, String page) {
        GuiRFToolsManual.locatePage = page;
        player.openGui(RFToolsControl.instance, bookIndex, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
