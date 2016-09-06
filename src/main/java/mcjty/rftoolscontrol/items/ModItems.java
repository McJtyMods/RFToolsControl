package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.items.manual.RFToolsControlManualItem;
import mcjty.rftoolscontrol.items.variablemodule.VariableModuleItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    public static RFToolsControlManualItem rfToolsControlManualItem;
    public static ProgramCardItem programCardItem;
    public static CPUCoreItem cpuCoreB500Item;
    public static CPUCoreItem cpuCoreS1000Item;
    public static CPUCoreItem cpuCoreEX2000Item;
    public static RAMChipItem ramChipItem;
    public static NetworkCardItem networkCardItem;
    public static CardBaseItem cardBaseItem;

    public static VariableModuleItem variableModuleItem;
    public static InteractionModuleItem interactionModuleItem;

    public static void init() {
        rfToolsControlManualItem = new RFToolsControlManualItem();
        programCardItem = new ProgramCardItem();
        cpuCoreB500Item = new CPUCoreItem("cpu_core_500", 0);
        cpuCoreS1000Item = new CPUCoreItem("cpu_core_1000", 1);
        cpuCoreEX2000Item = new CPUCoreItem("cpu_core_2000", 2);
        ramChipItem = new RAMChipItem();
        networkCardItem = new NetworkCardItem();
        cardBaseItem = new CardBaseItem();
        variableModuleItem = new VariableModuleItem();
        interactionModuleItem = new InteractionModuleItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        rfToolsControlManualItem.initModel();
        programCardItem.initModel();
        cpuCoreB500Item.initModel();
        cpuCoreS1000Item.initModel();
        cpuCoreEX2000Item.initModel();
        ramChipItem.initModel();
        networkCardItem.initModel();
        cardBaseItem.initModel();
        variableModuleItem.initModel();
        interactionModuleItem.initModel();
    }
}
