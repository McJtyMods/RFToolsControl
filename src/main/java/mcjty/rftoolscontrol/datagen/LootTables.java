package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
//        lootTables.put(BuilderSetup.BUILDER.get(), createStandardTable("builder", BuilderSetup.BUILDER.get()));
//        lootTables.put(ShieldSetup.TEMPLATE_YELLOW.get(), createSimpleTable("template_yellow", ShieldSetup.TEMPLATE_YELLOW.get()));
    }

    @Override
    public String getName() {
        return "RFToolsControl LootTables";
    }
}
