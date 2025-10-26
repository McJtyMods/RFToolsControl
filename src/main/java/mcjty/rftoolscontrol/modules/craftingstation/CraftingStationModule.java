package mcjty.rftoolscontrol.modules.craftingstation;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationBlock;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.data.CraftingStationData;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.RFToolsControl.tab;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class CraftingStationModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, CraftingStationTileEntity> CRAFTING_STATION = RBLOCKS.registerBlock(
            "craftingstation",
            CraftingStationTileEntity.class,
            CraftingStationBlock::new,
            block -> new BlockItem(block.get(), Registration.createStandardProperties()),
            CraftingStationTileEntity::new
    );
    public static final Supplier<MenuType<GenericContainer>> CRAFTING_STATION_CONTAINER = CONTAINERS.register("craftingstation", GenericContainer::createContainerType);

    public static final Supplier<AttachmentType<CraftingStationData>> CRAFTING_STATION_DATA = ATTACHMENT_TYPES.register(
            "craftingstation_data", () -> AttachmentType.builder(CraftingStationData::createDefault)
                    .serialize(CraftingStationData.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CraftingStationData>> ITEM_CRAFTING_STATION_DATA = COMPONENTS.registerComponentType(
            "craftingstation_data",
            builder -> builder
                    .persistent(CraftingStationData.CODEC)
                    .networkSynchronized(CraftingStationData.STREAM_CODEC));

    public CraftingStationModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiCraftingStation.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider registries) {
        dataGen.add(
                Dob.blockBuilder(CRAFTING_STATION)
                        .ironPickaxeTags()
                        .parentedItem("block/craftingstation")
                        .standardLoot(ITEM_CRAFTING_STATION_DATA.get())
                        .blockState(p -> p.orientedBlock(CRAFTING_STATION.block().get(), p.frontBasedModel("craftingstation", p.modLoc("block/machinecraftingstation"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('C', Items.CRAFTING_TABLE)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rMr", "CFC", "rMr")
        );
    }
}
