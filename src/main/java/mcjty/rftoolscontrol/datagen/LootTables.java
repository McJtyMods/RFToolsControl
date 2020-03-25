package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(Registration.PROCESSOR.get());
        addStandardTable(Registration.PROGRAMMER.get());
        addStandardTable(Registration.WORKBENCH.get());
        addStandardTable(Registration.NODE.get());
        addStandardTable(Registration.MULTITANK.get());
        addStandardTable(Registration.CRAFTING_STATION.get());
    }

    @Override
    public String getName() {
        return "RFToolsControl LootTables";
    }
}
