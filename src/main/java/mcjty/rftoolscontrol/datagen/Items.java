package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup;
import mcjty.rftoolscontrol.modules.multitank.MultiTankSetup;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerSetup;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsControl.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(ProcessorSetup.PROCESSOR.get(), "block/processor");
        parentedBlock(ProgrammerSetup.PROGRAMMER.get(), "block/programmer");
        parentedBlock(VariousSetup.WORKBENCH.get(), "block/workbench");
        parentedBlock(VariousSetup.NODE.get(), "block/node");
        parentedBlock(CraftingStationSetup.CRAFTING_STATION.get(), "block/craftingstation");
        parentedBlock(MultiTankSetup.MULTITANK.get(), "block/tank");

        itemGenerated(VariousSetup.PROGRAM_CARD.get(), "item/programcard");
        itemGenerated(ProcessorSetup.CPU_CORE_500.get(), "item/cpucoreb500");
        itemGenerated(ProcessorSetup.CPU_CORE_1000.get(), "item/cpucores1000");
        itemGenerated(ProcessorSetup.CPU_CORE_2000.get(), "item/cpucoreex2000");
        itemGenerated(ProcessorSetup.RAM_CHIP.get(), "item/ramchip");
        itemGenerated(ProcessorSetup.NETWORK_CARD.get(), "item/networkcard");
        itemGenerated(ProcessorSetup.ADVANCED_NETWORK_CARD.get(), "item/advancednetworkcard");
        itemGenerated(VariousSetup.CARD_BASE.get(), "item/cardbase");
        itemGenerated(VariousSetup.TOKEN.get(), "item/token");
        itemGenerated(ProcessorSetup.NETWORK_IDENTIFIER.get(), "item/networkidentifier");
        itemGenerated(ProcessorSetup.GRAPHICS_CARD.get(), "item/graphicscard");
        itemGenerated(VariousSetup.VARIABLE_MODULE.get(), "item/variablemoduleitem");
        itemGenerated(VariousSetup.INTERACTION_MODULE.get(), "item/interactionmoduleitem");
        itemGenerated(VariousSetup.CONSOLE_MODULE.get(), "item/consolemoduleitem");
        itemGenerated(VariousSetup.VECTORART_MODULE.get(), "item/vectorartmoduleitem");
        itemGenerated(VariousSetup.TABLET_PROCESSOR.get(), "item/tablet_processor");
    }

    @Override
    public String getName() {
        return "RFTools Control Item Models";
    }
}
