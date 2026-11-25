package mcjty.rftoolscontrol.modules.multitank;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredHolder;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankBlock;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.client.GuiMultiTank;
import mcjty.rftoolscontrol.modules.multitank.data.MultiTankData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;

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

    public static final AttachmentType<MultiTankData> MULTITANK_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "multitank_data"),
            builder -> builder.initializer(MultiTankData::createDefault).persistent(MultiTankData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MultiTankData>> ITEM_MULTITANK_DATA = COMPONENTS.registerComponentType(
            "multitank_data",
            builder -> builder
                    .persistent(MultiTankData.CODEC)
                    .networkSynchronized(MultiTankData.STREAM_CODEC));

    @Override
    public void init() {

    }

    @Override
    public void initClient() {
        registerMenuScreens();
    }

    private void registerMenuScreens() {
        GuiMultiTank.register();
    }

    @Override
    public void initConfig() {

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
