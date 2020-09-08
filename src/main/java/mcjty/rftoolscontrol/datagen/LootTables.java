package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(ProcessorModule.PROCESSOR.get());
        addStandardTable(ProgrammerModule.PROGRAMMER.get());
        addStandardTable(VariousModule.WORKBENCH.get());
        addStandardTable(VariousModule.NODE.get());
        addStandardTable(MultiTankModule.MULTITANK.get());
        addStandardTable(CraftingStationModule.CRAFTING_STATION.get());
    }

    @Override
    public String getName() {
        return "RFToolsControl LootTables";
    }
}
