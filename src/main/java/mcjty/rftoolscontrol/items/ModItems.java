package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.items.manual.RFToolsControlManualItem;
import mcjty.rftoolscontrol.items.variablemodule.VariableModuleItem;
import mcjty.rftoolscontrol.items.vectorartmodule.VectorArtModuleItem;



public class ModItems {

    public static RFToolsControlManualItem rfToolsControlManualItem;
    public static ProgramCardItem programCardItem;
    public static CPUCoreItem cpuCoreB500Item;
    public static CPUCoreItem cpuCoreS1000Item;
    public static CPUCoreItem cpuCoreEX2000Item;
    public static RAMChipItem ramChipItem;
    public static NetworkCardItem networkCardItem;
    public static NetworkCardItem advancedNetworkCardItem;
    public static CardBaseItem cardBaseItem;
    public static TokenItem tokenItem;
    public static NetworkIdentifierItem networkIdentifierItem;
    public static GraphicsCardItem graphicsCardItem;

    public static VariableModuleItem variableModuleItem;
    public static InteractionModuleItem interactionModuleItem;
    public static ConsoleModuleItem consoleModuleItem;
    public static VectorArtModuleItem vectorArtModuleItem;

    public static void init() {
        rfToolsControlManualItem = new RFToolsControlManualItem();
        programCardItem = new ProgramCardItem();
        cpuCoreB500Item = new CPUCoreItem("cpu_core_500", 0);
        cpuCoreS1000Item = new CPUCoreItem("cpu_core_1000", 1);
        cpuCoreEX2000Item = new CPUCoreItem("cpu_core_2000", 2);
        ramChipItem = new RAMChipItem();
        networkCardItem = new NetworkCardItem("network_card", NetworkCardItem.TIER_NORMAL);
        advancedNetworkCardItem = new NetworkCardItem("advanced_network_card", NetworkCardItem.TIER_ADVANCED);
        cardBaseItem = new CardBaseItem();
        tokenItem = new TokenItem();
        networkIdentifierItem = new NetworkIdentifierItem();
        graphicsCardItem = new GraphicsCardItem();

        variableModuleItem = new VariableModuleItem();
        interactionModuleItem = new InteractionModuleItem();
        consoleModuleItem = new ConsoleModuleItem();
        vectorArtModuleItem = new VectorArtModuleItem();
    }
}
