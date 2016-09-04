package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.items.manual.RFToolsControlManualItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    public static RFToolsControlManualItem rfToolsControlManualItem;
    public static ProgramCardItem programCardItem;
    public static CPUCoreItem cpuCoreEX2000Item;
    public static RAMChipItem ramChipItem;
    public static NetworkCardItem networkCardItem;

    public static void init() {
        rfToolsControlManualItem = new RFToolsControlManualItem();
        programCardItem = new ProgramCardItem();
        cpuCoreEX2000Item = new CPUCoreItem();
        ramChipItem = new RAMChipItem();
        networkCardItem = new NetworkCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        rfToolsControlManualItem.initModel();
        programCardItem.initModel();
        cpuCoreEX2000Item.initModel();
        ramChipItem.initModel();
        networkCardItem.initModel();
    }
}
