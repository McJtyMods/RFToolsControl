package mcjty.rftoolscontrol;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import mcjty.lib.base.ModBase;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.rftoolscontrol.api.registry.IFunctionRegistry;
import mcjty.rftoolscontrol.api.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.items.manual.GuiRFToolsManual;
import mcjty.rftoolscontrol.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = RFToolsControl.MODID, name="RFTools Control", dependencies =
        "required-after:Forge@["+ RFToolsControl.MIN_FORGE_VER+
        ",);required-after:rftools@["+RFToolsControl.MIN_RFTOOLS_VER+
        ",);required-after:McJtyLib@["+ RFToolsControl.MIN_MCJTYLIB_VER+",)",
        version = RFToolsControl.VERSION)
public class RFToolsControl implements ModBase {
    public static final String MODID = "rftoolscontrol";
    public static final String VERSION = "1.5.1";
    public static final String MIN_FORGE_VER = "12.16.1.1896";
    public static final String MIN_MCJTYLIB_VER = "1.10-2.2.0";
    public static final String MIN_RFTOOLS_VER = "1.10-5.22";

    @SidedProxy(clientSide="mcjty.rftoolscontrol.proxy.ClientProxy", serverSide="mcjty.rftoolscontrol.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static RFToolsControl instance;

    public static boolean mcmpPresent = false;

    /** This is used to keep track of GUIs that we make*/
    private static int modGuiIndex = 0;
    public static final int GUI_MANUAL_CONTROL = modGuiIndex++;
    public static final int GUI_PROGRAMMER = modGuiIndex++;
    public static final int GUI_PROCESSOR = modGuiIndex++;
    public static final int GUI_NODE = modGuiIndex++;
    public static final int GUI_CRAFTINGSTATION = modGuiIndex++;
    public static final int GUI_CRAFTINGCARD = modGuiIndex++;
    public static final int GUI_WORKBENCH = modGuiIndex++;
    public static final int GUI_TANK = modGuiIndex++;

    public static CreativeTabs tabRFToolsControl = new CreativeTabs("RFToolsControl") {
        @SuppressWarnings("NullableProblems")
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return ModItems.rfToolsControlManualItem;
        }
    };

    public static final String SHIFT_MESSAGE = "<Press Shift>";

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        mcmpPresent = Loader.isModLoaded("mcmultipart");

        this.proxy.preInit(e);
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        FMLInterModComms.sendFunctionMessage("rftools", "getScreenModuleRegistry", "mcjty.rftoolscontrol.rftoolssupport.RFToolsSupport$GetScreenModuleRegistry");

//        FMLInterModComms.sendFunctionMessage("rftools", "getTeleportationManager", "mcjty.RFToolsControl.RFToolsControl$GetTeleportationManager");
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.RFToolsControl.theoneprobe.TheOneProbeSupport");
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        this.proxy.init(e);

//        Achievements.init();
        // @todo
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
    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        this.proxy.postInit(e);
    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void openManual(EntityPlayer player, int bookIndex, String page) {
        GuiRFToolsManual.locatePage = page;
        player.openGui(RFToolsControl.instance, bookIndex, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
