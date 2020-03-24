package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsControl.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
//        singleTextureBlock(ShieldSetup.TEMPLATE_BLUE.get(), "blue_shield_template", "block/shieldtemplate");
//        horizontalOrientedBlock(BuilderSetup.BUILDER.get(), frontBasedModel("builder", modLoc("block/machinebuilder")));
    }
}
