package mcjty.rftoolscontrol.setup;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationBlock;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationContainer;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankBlock;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankContainer;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity;
import mcjty.rftoolscontrol.blocks.node.NodeBlock;
import mcjty.rftoolscontrol.blocks.node.NodeContainer;
import mcjty.rftoolscontrol.blocks.node.NodeTileEntity;
import mcjty.rftoolscontrol.blocks.processor.ProcessorBlock;
import mcjty.rftoolscontrol.blocks.processor.ProcessorContainer;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerBlock;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerContainer;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerTileEntity;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchBlock;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchContainer;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchTileEntity;
import mcjty.rftoolscontrol.items.*;
import mcjty.rftoolscontrol.items.consolemodule.ConsoleModuleItem;
import mcjty.rftoolscontrol.items.interactionmodule.InteractionModuleItem;
import mcjty.rftoolscontrol.items.variablemodule.VariableModuleItem;
import mcjty.rftoolscontrol.items.vectorartmodule.VectorArtModuleItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.rftoolscontrol.RFToolsControl.MODID;

public class Registration {

    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<BaseBlock> CRAFTING_STATION = BLOCKS.register("craftingstation", CraftingStationBlock::new);
    public static final RegistryObject<Item> CRAFTING_STATION_ITEM = ITEMS.register("craftingstation", () -> new BlockItem(CRAFTING_STATION.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<CraftingStationTileEntity>> CRAFTING_STATION_TILE = TILES.register("craftingstation", () -> TileEntityType.Builder.create(CraftingStationTileEntity::new, CRAFTING_STATION.get()).build(null));
    public static final RegistryObject<ContainerType<CraftingStationContainer>> CRAFTING_STATION_CONTAINER = CONTAINERS.register("craftingstation", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> MULTITANK = BLOCKS.register("tank", MultiTankBlock::new);
    public static final RegistryObject<Item> MULTITANK_ITEM = ITEMS.register("tank", () -> new BlockItem(MULTITANK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<MultiTankTileEntity>> MULTITANK_TILE = TILES.register("tank", () -> TileEntityType.Builder.create(MultiTankTileEntity::new, MULTITANK.get()).build(null));
    public static final RegistryObject<ContainerType<MultiTankContainer>> MULTITANK_CONTAINER = CONTAINERS.register("tank", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> NODE = BLOCKS.register("node", NodeBlock::new);
    public static final RegistryObject<Item> NODE_ITEM = ITEMS.register("node", () -> new BlockItem(NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<NodeTileEntity>> NODE_TILE = TILES.register("node", () -> TileEntityType.Builder.create(NodeTileEntity::new, NODE.get()).build(null));
    public static final RegistryObject<ContainerType<NodeContainer>> NODE_CONTAINER = CONTAINERS.register("node", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> PROCESSOR = BLOCKS.register("processor", ProcessorBlock::new);
    public static final RegistryObject<Item> PROCESSOR_ITEM = ITEMS.register("processor", () -> new BlockItem(PROCESSOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProcessorTileEntity>> PROCESSOR_TILE = TILES.register("processor", () -> TileEntityType.Builder.create(ProcessorTileEntity::new, PROCESSOR.get()).build(null));
    public static final RegistryObject<ContainerType<ProcessorContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> PROGRAMMER = BLOCKS.register("programmer", ProgrammerBlock::new);
    public static final RegistryObject<Item> PROGRAMMER_ITEM = ITEMS.register("programmer", () -> new BlockItem(PROGRAMMER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProgrammerTileEntity>> PROGRAMMER_TILE = TILES.register("programmer", () -> TileEntityType.Builder.create(ProgrammerTileEntity::new, PROGRAMMER.get()).build(null));
    public static final RegistryObject<ContainerType<ProgrammerContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> WORKBENCH = BLOCKS.register("workbench", WorkbenchBlock::new);
    public static final RegistryObject<Item> WORKBENCH_ITEM = ITEMS.register("workbench", () -> new BlockItem(WORKBENCH.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<WorkbenchTileEntity>> WORKBENCH_TILE = TILES.register("workbench", () -> TileEntityType.Builder.create(WorkbenchTileEntity::new, WORKBENCH.get()).build(null));
    public static final RegistryObject<ContainerType<WorkbenchContainer>> WORKBENCH_CONTAINER = CONTAINERS.register("workbench", GenericContainer::createContainerType);

    public static final RegistryObject<ProgramCardItem> PROGRAM_CARD = ITEMS.register("program_card", ProgramCardItem::new);
    public static final RegistryObject<CPUCoreItem> CPU_CORE_500 = ITEMS.register("cpu_core_500", () -> new CPUCoreItem(0));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_1000 = ITEMS.register("cpu_core_1000", () -> new CPUCoreItem(1));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_2000 = ITEMS.register("cpu_core_2000", () -> new CPUCoreItem(2));
    public static final RegistryObject<RAMChipItem> RAM_CHIP = ITEMS.register("ram_chip", RAMChipItem::new);
    public static final RegistryObject<NetworkCardItem> NETWORK_CARD = ITEMS.register("network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_NORMAL));
    public static final RegistryObject<NetworkCardItem> ADVANCED_NETWORK_CARD = ITEMS.register("advanced_network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_ADVANCED));
    public static final RegistryObject<CardBaseItem> CARD_BASE = ITEMS.register("card_base", CardBaseItem::new);
    public static final RegistryObject<TokenItem> TOKEN = ITEMS.register("token", TokenItem::new);
    public static final RegistryObject<NetworkIdentifierItem> NETWORK_IDENTIFIER = ITEMS.register("network_identifier", NetworkIdentifierItem::new);
    public static final RegistryObject<GraphicsCardItem> GRAPHICS_CARD = ITEMS.register("graphics_card", GraphicsCardItem::new);
    public static final RegistryObject<VariableModuleItem> VARIABLE_MODULE = ITEMS.register("variable_module", VariableModuleItem::new);
    public static final RegistryObject<InteractionModuleItem> INTERACTION_MODULE = ITEMS.register("interaction_module", InteractionModuleItem::new);
    public static final RegistryObject<ConsoleModuleItem> CONSOLE_MODULE = ITEMS.register("console_module", ConsoleModuleItem::new);
    public static final RegistryObject<VectorArtModuleItem> VECTORART_MODULE = ITEMS.register("vectorart_module", VectorArtModuleItem::new);


    public static Item.Properties createStandardProperties() {
        return new Item.Properties().group(RFToolsControl.setup.getTab());
    }
}
