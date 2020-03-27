package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.items.*;
import mcjty.rftoolscontrol.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.items.variablemodule.VariableModuleItem;
import mcjty.rftoolscontrol.items.vectorartmodule.VectorArtModuleItem;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsControl.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(Registration.PROCESSOR.get(), "block/processor");
        parentedBlock(Registration.PROGRAMMER.get(), "block/programmer");
        parentedBlock(Registration.WORKBENCH.get(), "block/workbench");
        parentedBlock(Registration.NODE.get(), "block/node");
        parentedBlock(Registration.CRAFTING_STATION.get(), "block/craftingstation");
        parentedBlock(Registration.MULTITANK.get(), "block/tank");

        itemGenerated(Registration.PROGRAM_CARD.get(), "item/programcard");
        itemGenerated(Registration.CPU_CORE_500.get(), "item/cpucoreb500");
        itemGenerated(Registration.CPU_CORE_1000.get(), "item/cpucores1000");
        itemGenerated(Registration.CPU_CORE_2000.get(), "item/cpucoreex2000");
        itemGenerated(Registration.RAM_CHIP.get(), "item/ramchip");
        itemGenerated(Registration.NETWORK_CARD.get(), "item/networkcard");
        itemGenerated(Registration.ADVANCED_NETWORK_CARD.get(), "item/advancednetworkcard");
        itemGenerated(Registration.CARD_BASE.get(), "item/cardbase");
        itemGenerated(Registration.TOKEN.get(), "item/token");
        itemGenerated(Registration.NETWORK_IDENTIFIER.get(), "item/networkidentifier");
        itemGenerated(Registration.GRAPHICS_CARD.get(), "item/graphicscard");
        itemGenerated(Registration.VARIABLE_MODULE.get(), "item/variablemoduleitem");
        itemGenerated(Registration.INTERACTION_MODULE.get(), "item/interactionmoduleitem");
        itemGenerated(Registration.CONSOLE_MODULE.get(), "item/consolemoduleitem");
        itemGenerated(Registration.VECTORART_MODULE.get(), "item/vectorartmoduleitem");
    }

    @Override
    public String getName() {
        return "RFTools Control Item Models";
    }
}
