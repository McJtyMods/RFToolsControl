package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.items.manual.RFToolsControlManualItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    public static RFToolsControlManualItem rfToolsControlManualItem;
    public static ProgramCardItem programCardItem;

    public static void init() {
        rfToolsControlManualItem = new RFToolsControlManualItem();
        programCardItem = new ProgramCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        rfToolsControlManualItem.initModel();
        programCardItem.initModel();
    }
}
