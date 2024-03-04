package mcjty.rftoolscontrol.modules.programmer;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerBlock;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.modules.programmer.client.GuiProgrammer;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.RFToolsControl.tab;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProgrammerModule implements IModule {

    public static final DeferredBlock<BaseBlock> PROGRAMMER = BLOCKS.register("programmer", ProgrammerBlock::new);
    public static final Supplier<BlockEntityType<ProgrammerTileEntity>> TYPE_PROGRAMMER = TILES.register("programmer", () -> BlockEntityType.Builder.of(ProgrammerTileEntity::new, PROGRAMMER.get()).build(null));
    public static final DeferredItem<Item> PROGRAMMER_ITEM = ITEMS.register("programmer", tab(() -> new BlockItem(PROGRAMMER.get(), Registration.createStandardProperties())));
    public static final Supplier<MenuType<GenericContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiProgrammer.register();
        });
        ParameterEditors.init();
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(PROGRAMMER)
                        .ironPickaxeTags()
                        .parentedItem("block/programmer")
                        .standardLoot(TYPE_PROGRAMMER)
                        .blockState(p -> p.orientedBlock(PROGRAMMER.get(), p.frontBasedModel("programmer", p.modLoc("block/machineprogrammer"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('q', Tags.Items.GEMS_QUARTZ)
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rqr", "pFp", "rqr")
        );
    }
}
