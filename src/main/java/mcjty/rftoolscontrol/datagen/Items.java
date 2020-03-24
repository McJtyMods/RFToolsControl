package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, RFToolsControl.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
//        parentedBlock(ShieldSetup.TEMPLATE_BLUE.get(), "block/blue_shield_template");
//        itemGenerated(BuilderSetup.SHAPE_CARD_DEF.get(), "item/shapecarditem");
    }

    @Override
    public String getName() {
        return "RFTools Control Item Models";
    }
}
