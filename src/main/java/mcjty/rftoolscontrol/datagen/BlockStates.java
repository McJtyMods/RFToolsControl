package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsControl.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        orientedBlock(ProcessorModule.PROCESSOR.get(), frontBasedModel("processor", modLoc("block/machineprocessoron")));
        orientedBlock(VariousModule.WORKBENCH.get(), frontBasedModel("workbench", modLoc("block/machineworkbench")));
        orientedBlock(ProgrammerModule.PROGRAMMER.get(), frontBasedModel("programmer", modLoc("block/machineprogrammer")));
        orientedBlock(VariousModule.NODE.get(), frontBasedModel("node", modLoc("block/machinenode")));
        orientedBlock(CraftingStationModule.CRAFTING_STATION.get(), frontBasedModel("craftingstation", modLoc("block/machinecraftingstation")));

        // @todo 1.15
        //        orientedBlock(Registration.MULTITANK.get(), frontBasedModel("multitank", modLoc("block/machinecraftingstation")));
    }
}
