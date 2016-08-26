package mcjty.rftoolscontrol.proxy;

import mcjty.rftoolscontrol.ModCrafting;
import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import mcjty.rftoolscontrol.gui.GuiProxy;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Level;

import java.io.File;

public abstract class CommonProxy {

    public static File modConfigDir;
    private Configuration mainConfig;

    public void preInit(FMLPreInitializationEvent e) {
        GeneralConfig.preInit(e);

        modConfigDir = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "rftools", "control.cfg"));
        readMainConfig();

        SimpleNetworkWrapper network = PacketHandler.registerMessages(RFToolsControl.MODID, "rftoolsctrl");
        RFToolsCtrlMessages.registerNetworkMessages(network);

        ModItems.init();
        ModBlocks.init();
    }

    private void readMainConfig() {
        Configuration cfg = mainConfig;
        try {
            cfg.load();
            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");

            GeneralConfiguration.init(cfg);
        } catch (Exception e1) {
            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(RFToolsControl.instance, new GuiProxy());
//        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
//        MinecraftForge.EVENT_BUS.register(new DimensionTickEvent());
        ModCrafting.init();
    }

    public void postInit(FMLPostInitializationEvent e) {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }


        mainConfig = null;
        WrenchChecker.init();
    }

}
