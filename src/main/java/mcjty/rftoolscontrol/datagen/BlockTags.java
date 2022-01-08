package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseBlockTagsProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class BlockTags extends BaseBlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, RFToolsControl.MODID, helper);
    }

    @Override
    protected void addTags() {
        ironPickaxe(
                CraftingStationModule.CRAFTING_STATION,
                MultiTankModule.MULTITANK,
                ProcessorModule.PROCESSOR,
                ProgrammerModule.PROGRAMMER,
                VariousModule.NODE, VariousModule.WORKBENCH
        );
    }

    @Override
    @Nonnull
    public String getName() {
        return "RFToolsControl Tags";
    }
}
