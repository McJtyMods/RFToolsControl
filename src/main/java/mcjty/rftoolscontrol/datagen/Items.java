package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsControl.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedBlock(ProcessorModule.PROCESSOR.get(), "block/processor");
        parentedBlock(ProgrammerModule.PROGRAMMER.get(), "block/programmer");
        parentedBlock(VariousModule.WORKBENCH.get(), "block/workbench");
        parentedBlock(VariousModule.NODE.get(), "block/node");
        parentedBlock(CraftingStationModule.CRAFTING_STATION.get(), "block/craftingstation");
        parentedBlock(MultiTankModule.MULTITANK.get(), "block/tank");

        itemGenerated(VariousModule.PROGRAM_CARD.get(), "item/programcard");
        itemGenerated(ProcessorModule.CPU_CORE_500.get(), "item/cpucoreb500");
        itemGenerated(ProcessorModule.CPU_CORE_1000.get(), "item/cpucores1000");
        itemGenerated(ProcessorModule.CPU_CORE_2000.get(), "item/cpucoreex2000");
        itemGenerated(ProcessorModule.RAM_CHIP.get(), "item/ramchip");
        itemGenerated(ProcessorModule.NETWORK_CARD.get(), "item/networkcard");
        itemGenerated(ProcessorModule.ADVANCED_NETWORK_CARD.get(), "item/advancednetworkcard");
        itemGenerated(VariousModule.CARD_BASE.get(), "item/cardbase");
        itemGenerated(VariousModule.TOKEN.get(), "item/token");
        itemGenerated(ProcessorModule.NETWORK_IDENTIFIER.get(), "item/networkidentifier");
        itemGenerated(ProcessorModule.GRAPHICS_CARD.get(), "item/graphicscard");
        itemGenerated(VariousModule.VARIABLE_MODULE.get(), "item/variablemoduleitem");
        itemGenerated(VariousModule.INTERACTION_MODULE.get(), "item/interactionmoduleitem");
        itemGenerated(VariousModule.CONSOLE_MODULE.get(), "item/consolemoduleitem");
        itemGenerated(VariousModule.VECTORART_MODULE.get(), "item/vectorartmoduleitem");
        itemGenerated(VariousModule.TABLET_PROCESSOR.get(), "item/tablet_processor");
    }

    @Override
    public String getName() {
        return "RFTools Control Item Models";
    }
}
