package mcjty.rftoolscontrol.modules.multitank;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankBlock;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.client.GuiMultiTank;
import mcjty.rftoolscontrol.modules.multitank.data.MultiTankData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class MultiTankModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, MultiTankTileEntity> MULTITANK = RBLOCKS.registerBlock(
            "tank",
            MultiTankTileEntity.class,
            MultiTankBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            MultiTankTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> MULTITANK_CONTAINER = CONTAINERS.register("tank", GenericContainer::createContainerType);

    public static final Supplier<AttachmentType<MultiTankData>> MULTITANK_DATA = ATTACHMENT_TYPES.register(
            "multitank_data", () -> AttachmentType.builder(MultiTankData::createDefault)
                    .serialize(MapCodec.assumeMapUnsafe(MultiTankData.CODEC))
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MultiTankData>> ITEM_MULTITANK_DATA = COMPONENTS.registerComponentType(
            "multitank_data",
            builder -> builder
                    .persistent(MultiTankData.CODEC)
                    .networkSynchronized(MultiTankData.STREAM_CODEC));

    public MultiTankModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiMultiTank.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider registries) {
        dataGen.add(
                Dob.blockBuilder(MULTITANK)
                        .ironPickaxeTags()
                        .parentedItem("block/tank")
                        .standardLoot(ITEM_MULTITANK_DATA.get())
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "Fii", "iGG", "iGG")
        );
    }
}
