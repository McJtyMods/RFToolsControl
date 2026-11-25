package mcjty.rftoolscontrol.modules.processor;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredHolder;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorBlock;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.client.ProcessorRenderer;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorCardInfoData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorCoreData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorCraftingData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorEventData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorExtraData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorGraphicsOperationsData;
import mcjty.rftoolscontrol.modules.processor.data.ProcessorSettingsData;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolscontrol.RFToolsControl.tab;
import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProcessorModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, ProcessorTileEntity> PROCESSOR = RBLOCKS.registerBlock(
            "processor",
            ProcessorTileEntity.class,
            ProcessorBlock::new,
            (block, properties) -> new BaseBlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ProcessorTileEntity::new
    );
    public static final Supplier<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);
    public static final Supplier<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER_REMOTE = CONTAINERS.register("processor_remote",
            () -> GenericContainer.createRemoteContainerType(ProcessorTileEntity::new, ProcessorContainer::createRemote, ProcessorContainer.SLOTS));

    public static final DeferredItem<CPUCoreItem> CPU_CORE_500 = ITEMS.register("cpu_core_500", tab(properties -> new CPUCoreItem(properties, 0)));
    public static final DeferredItem<CPUCoreItem> CPU_CORE_1000 = ITEMS.register("cpu_core_1000", tab(properties -> new CPUCoreItem(properties, 1)));
    public static final DeferredItem<CPUCoreItem> CPU_CORE_2000 = ITEMS.register("cpu_core_2000", tab(properties -> new CPUCoreItem(properties, 2)));
    public static final DeferredItem<RAMChipItem> RAM_CHIP = ITEMS.register("ram_chip", tab(RAMChipItem::new));
    public static final DeferredItem<NetworkCardItem> NETWORK_CARD = ITEMS.register("network_card", tab(properties -> new NetworkCardItem(properties, NetworkCardItem.TIER_NORMAL)));
    public static final DeferredItem<NetworkCardItem> ADVANCED_NETWORK_CARD = ITEMS.register("advanced_network_card", tab(properties -> new NetworkCardItem(properties, NetworkCardItem.TIER_ADVANCED)));
    public static final DeferredItem<NetworkIdentifierItem> NETWORK_IDENTIFIER = ITEMS.register("network_identifier", tab(NetworkIdentifierItem::new));
    public static final DeferredItem<GraphicsCardItem> GRAPHICS_CARD = ITEMS.register("graphics_card", tab(GraphicsCardItem::new));

    // Data components for processor saved data blocks
    public static final AttachmentType<ProcessorGraphicsOperationsData> PROCESSOR_GRAPHICS_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_graphics_data"),
            builder -> builder.initializer(() -> ProcessorGraphicsOperationsData.DEFAULT).persistent(ProcessorGraphicsOperationsData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorGraphicsOperationsData>> ITEM_PROCESSOR_GRAPHICS_DATA = COMPONENTS.registerComponentType(
            "processor_graphics_data",
            builder -> builder.persistent(ProcessorGraphicsOperationsData.CODEC).networkSynchronized(ProcessorGraphicsOperationsData.STREAM_CODEC)
    );

    public static final AttachmentType<ProcessorCardInfoData> PROCESSOR_CARD_INFO_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_card_info_data"),
            builder -> builder.initializer(() -> ProcessorCardInfoData.DEFAULT).persistent(ProcessorCardInfoData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorCardInfoData>> ITEM_PROCESSOR_CARD_INFO_DATA = COMPONENTS.registerComponentType(
            "processor_card_info_data",
            builder -> builder.persistent(ProcessorCardInfoData.CODEC).networkSynchronized(ProcessorCardInfoData.STREAM_CODEC)
    );

    public static final AttachmentType<ProcessorCoreData> PROCESSOR_CORE_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_core_data"),
            builder -> builder.initializer(() -> ProcessorCoreData.DEFAULT).persistent(ProcessorCoreData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorCoreData>> ITEM_PROCESSOR_CORE_DATA = COMPONENTS.registerComponentType(
            "processor_core_data",
            builder -> builder.persistent(ProcessorCoreData.CODEC).networkSynchronized(ProcessorCoreData.STREAM_CODEC)
    );

    public static final AttachmentType<ProcessorEventData> PROCESSOR_EVENTS_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_events_data"),
            builder -> builder.initializer(ProcessorEventData::empty).persistent(ProcessorEventData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorEventData>> ITEM_PROCESSOR_EVENTS_DATA = COMPONENTS.registerComponentType(
            "processor_events_data",
            builder -> builder.persistent(ProcessorEventData.CODEC).networkSynchronized(ProcessorEventData.STREAM_CODEC)
    );

    public static final AttachmentType<ProcessorCraftingData> PROCESSOR_CRAFTING_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_crafting_data"),
            builder -> builder.initializer(() -> ProcessorCraftingData.DEFAULT).persistent(ProcessorCraftingData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorCraftingData>> ITEM_PROCESSOR_CRAFTING_DATA = COMPONENTS.registerComponentType(
            "processor_crafting_data",
            builder -> builder.persistent(ProcessorCraftingData.CODEC).networkSynchronized(ProcessorCraftingData.STREAM_CODEC)
    );

    // Extra data: network nodes and log messages
    public static final AttachmentType<ProcessorExtraData> PROCESSOR_EXTRA_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_extra_data"),
            builder -> builder.initializer(() -> ProcessorExtraData.DEFAULT).persistent(ProcessorExtraData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorExtraData>> ITEM_PROCESSOR_EXTRA_DATA = COMPONENTS.registerComponentType(
            "processor_extra_data",
            builder -> builder.persistent(ProcessorExtraData.CODEC).networkSynchronized(ProcessorExtraData.STREAM_CODEC)
    );

    // Settings data: HUD and exclusive mode
    public static final AttachmentType<ProcessorSettingsData> PROCESSOR_SETTINGS_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "processor_settings_data"),
            builder -> builder.initializer(() -> ProcessorSettingsData.DEFAULT).persistent(ProcessorSettingsData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProcessorSettingsData>> ITEM_PROCESSOR_SETTINGS_DATA = COMPONENTS.registerComponentType(
            "processor_settings_data",
            builder -> builder.persistent(ProcessorSettingsData.CODEC).networkSynchronized(ProcessorSettingsData.STREAM_CODEC)
    );

    @Override
    public void init() {

    }

    @Override
    public void initClient() {
        registerMenuScreens();
        ProcessorRenderer.register();
    }

    private void registerMenuScreens() {
        GuiProcessor.register();
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider registries) {
        dataGen.add(
                Dob.blockBuilder(PROCESSOR)
                        .ironPickaxeTags()
                        .parentedItem("block/processor")
                        .standardLoot(ITEM_PROCESSOR_CARD_INFO_DATA.get(), ITEM_PROCESSOR_CORE_DATA.get(),
                                ITEM_PROCESSOR_CRAFTING_DATA.get(), ITEM_PROCESSOR_EVENTS_DATA.get(), ITEM_PROCESSOR_EXTRA_DATA.get(),
                                ITEM_PROCESSOR_SETTINGS_DATA.get())
                        .blockState(p -> p.orientedBlock(PROCESSOR.block().get(), p.frontBasedModel("processor", p.modLoc("block/machineprocessoron"))))
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('q', TagKey.create(Registries.ITEM, Identifier.parse("c:gems/quartz")))
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rqr", "MFM", "rqr"),
                Dob.itemBuilder(CPU_CORE_500)
                        .generatedItem("item/cpucoreb500")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('g', TagKey.create(Registries.ITEM, Identifier.parse("c:nuggets/gold")))
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rgr", "pMp", "rgr"),
                Dob.itemBuilder(CPU_CORE_1000)
                        .generatedItem("item/cpucores1000")
                        .shaped(builder -> builder
                                        .define('M', CPU_CORE_500.get())
                                        .unlockedBy("core500", has(CPU_CORE_500.get())),
                                "rdr", "eMe", "rdr"),
                Dob.itemBuilder(CPU_CORE_2000)
                        .generatedItem("item/cpucoreex2000")
                        .shaped(builder -> builder
                                .define('s', mcjty.rftoolsbase.modules.various.VariousModule.DIMENSIONALSHARD.get())

                                        .define('M', CPU_CORE_1000.get())
                                        .unlockedBy("core1000", has(CPU_CORE_1000.get())),
                                "rsr", "sMs", "rsr"),
                Dob.itemBuilder(RAM_CHIP)
                        .generatedItem("item/ramchip")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "rrr", "pMp", "rrr"),
                Dob.itemBuilder(NETWORK_CARD)
                        .generatedItem("item/networkcard")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('g', TagKey.create(Registries.ITEM, Identifier.parse("c:nuggets/gold")))
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "ror", "gMg", "rrr"),
                Dob.itemBuilder(ADVANCED_NETWORK_CARD)
                        .generatedItem("item/advancednetworkcard")
                        .shaped(builder -> builder
                                        .define('M', NETWORK_CARD.get())
                                        .unlockedBy("network_card", has(NETWORK_CARD.get())),
                                "ror", "eMe", "ror"),
                Dob.itemBuilder(NETWORK_IDENTIFIER)
                        .generatedItem("item/networkidentifier")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('X', Items.REPEATER)
                                        .define('C', Items.COMPARATOR)
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                " C ", " M ", " X "),
                Dob.itemBuilder(GRAPHICS_CARD)
                        .generatedItem("item/graphicscard")
                        .shaped(builder -> builder
                                        .define('M', VariousModule.CARD_BASE.get())
                                        .define('q', TagKey.create(Registries.ITEM, Identifier.parse("c:gems/quartz")))
                                        .define('g', TagKey.create(Registries.ITEM, Identifier.parse("c:dusts/glowstone")))
                                        .unlockedBy("cardbase", has(VariousModule.CARD_BASE.get())),
                                "qqq", "rMr", "qgq")
        );
    }
}
