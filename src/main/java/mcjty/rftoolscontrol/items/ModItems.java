package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
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
    public static CraftingCardItem craftingCardItem;
    public static TokenItem tokenItem;

    public static VariableModuleItem variableModuleItem;
    public static InteractionModuleItem interactionModuleItem;
    public static ConsoleModuleItem consoleModuleItem;

    public static void init() {
        rfToolsControlManualItem = new RFToolsControlManualItem();
        programCardItem = new ProgramCardItem();
        cpuCoreB500Item = new CPUCoreItem("cpu_core_500", 0);
        cpuCoreS1000Item = new CPUCoreItem("cpu_core_1000", 1);
        cpuCoreEX2000Item = new CPUCoreItem("cpu_core_2000", 2);
        ramChipItem = new RAMChipItem();
        networkCardItem = new NetworkCardItem();
        cardBaseItem = new CardBaseItem();
        craftingCardItem = new CraftingCardItem();
        tokenItem = new TokenItem();

        variableModuleItem = new VariableModuleItem();
        interactionModuleItem = new InteractionModuleItem();
        consoleModuleItem = new ConsoleModuleItem();
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
        craftingCardItem.initModel();
        tokenItem.initModel();

        variableModuleItem.initModel();
        interactionModuleItem.initModel();
        consoleModuleItem.initModel();
    }
}
