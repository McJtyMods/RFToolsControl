package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.data.DataGenerator;

import javax.annotation.Nonnull;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(ProcessorModule.PROCESSOR.get(), ProcessorModule.PROCESSOR_TILE.get());
        addStandardTable(ProgrammerModule.PROGRAMMER.get(), ProgrammerModule.PROGRAMMER_TILE.get());
        addStandardTable(VariousModule.WORKBENCH.get(), VariousModule.WORKBENCH_TILE.get());
        addStandardTable(VariousModule.NODE.get(), VariousModule.NODE_TILE.get());
        addStandardTable(MultiTankModule.MULTITANK.get(), MultiTankModule.MULTITANK_TILE.get());
        addStandardTable(CraftingStationModule.CRAFTING_STATION.get(), CraftingStationModule.CRAFTING_STATION_TILE.get());
    }

    @Nonnull
    @Override
    public String getName() {
        return "RFToolsControl LootTables";
    }
}
