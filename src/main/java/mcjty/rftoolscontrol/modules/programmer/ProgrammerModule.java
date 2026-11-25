package mcjty.rftoolscontrol.modules.programmer;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerBlock;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.modules.programmer.client.GuiProgrammer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.setup.Registration.CONTAINERS;
import static mcjty.rftoolscontrol.setup.Registration.RBLOCKS;

public class ProgrammerModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, ProgrammerTileEntity> PROGRAMMER = RBLOCKS.registerBlock(
            "programmer",
            ProgrammerTileEntity.class,
            ProgrammerBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ProgrammerTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

    @Override
    public void init() {

    }

    @Override
    public void initClient() {
        registerMenuScreens();
        ParameterEditors.init();
    }

    private void registerMenuScreens() {
        GuiProgrammer.register();
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider registries) {
        dataGen.add(
                Dob.blockBuilder(PROGRAMMER)
                        .ironPickaxeTags()
                        .parentedItem("block/programmer")
                        .standardLoot()
                        .blockState(p -> p.orientedBlock(PROGRAMMER.block().get(), p.frontBasedModel("programmer", p.modLoc("block/machineprogrammer"))))
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('q', TagKey.create(Registries.ITEM, Identifier.parse("c:gems/quartz")))
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rqr", "pFp", "rqr")
        );
    }
}
