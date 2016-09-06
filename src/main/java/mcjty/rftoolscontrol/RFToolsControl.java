package mcjty.rftoolscontrol;

import mcjty.lib.base.ModBase;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
    public static final String VERSION = "0.0.1";
    public static final String MIN_FORGE_VER = "12.16.1.1896";
    public static final String MIN_MCJTYLIB_VER = "1.10-2.0.0beta2";
    public static final String MIN_RFTOOLS_VER = "1.10-5.20";

    @SidedProxy(clientSide="mcjty.rftoolscontrol.proxy.ClientProxy", serverSide="mcjty.rftoolscontrol.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static RFToolsControl instance;

    /** This is used to keep track of GUIs that we make*/
    private static int modGuiIndex = 0;
    public static final int GUI_MANUAL_CONTROL = modGuiIndex++;
    public static final int GUI_PROGRAMMER = modGuiIndex++;
    public static final int GUI_PROCESSOR = modGuiIndex++;
    public static final int GUI_NODE = modGuiIndex++;

    public static CreativeTabs tabRFToolsControl = new CreativeTabs("RFToolsControl") {
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
        this.proxy.preInit(e);
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        FMLInterModComms.sendFunctionMessage("rftools", "getScreenModuleRegistry", "mcjty.rftoolscontrol.rftoolssupport.RFToolsSupport$GetScreenModuleRegistry");

//        FMLInterModComms.sendFunctionMessage("rftools", "getTeleportationManager", "mcjty.RFToolsControl.RFToolsControl$GetTeleportationManager");
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.RFToolsControl.theoneprobe.TheOneProbeSupport");
    }

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
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
//        GuiRFToolsManual.locatePage = page;
//        player.openGui(RFToolsControl.instance, bookIndex, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
