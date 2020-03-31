package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerSetup;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsControl.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        orientedBlock(ProcessorSetup.PROCESSOR.get(), frontBasedModel("processor", modLoc("block/machineprocessoron")));
        orientedBlock(VariousSetup.WORKBENCH.get(), frontBasedModel("workbench", modLoc("block/machineworkbench")));
        orientedBlock(ProgrammerSetup.PROGRAMMER.get(), frontBasedModel("programmer", modLoc("block/machineprogrammer")));
        orientedBlock(VariousSetup.NODE.get(), frontBasedModel("node", modLoc("block/machinenode")));
        orientedBlock(CraftingStationSetup.CRAFTING_STATION.get(), frontBasedModel("craftingstation", modLoc("block/machinecraftingstation")));

        // @todo 1.15
        //        orientedBlock(Registration.MULTITANK.get(), frontBasedModel("multitank", modLoc("block/machinecraftingstation")));
    }
}
