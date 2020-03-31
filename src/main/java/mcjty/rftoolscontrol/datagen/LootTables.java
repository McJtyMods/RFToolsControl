package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup;
import mcjty.rftoolscontrol.modules.multitank.MultiTankSetup;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerSetup;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(ProcessorSetup.PROCESSOR.get());
        addStandardTable(ProgrammerSetup.PROGRAMMER.get());
        addStandardTable(VariousSetup.WORKBENCH.get());
        addStandardTable(VariousSetup.NODE.get());
        addStandardTable(MultiTankSetup.MULTITANK.get());
        addStandardTable(CraftingStationSetup.CRAFTING_STATION.get());
    }

    @Override
    public String getName() {
        return "RFToolsControl LootTables";
    }
}
