package mcjty.rftoolscontrol.setup;


import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationBlock;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity;
import mcjty.rftoolscontrol.blocks.node.NodeBlock;
import mcjty.rftoolscontrol.blocks.node.NodeTileEntity;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.blocks.programmer.ProgrammerTileEntity;
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
    public static final RegistryObject<ContainerType<GenericContainer>> CRAFTING_STATION_CONTAINER = CONTAINERS.register("craftingstation", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> MULTITANK = BLOCKS.register("tank", CraftingStationBlock::new);
    public static final RegistryObject<Item> MULTITANK_ITEM = ITEMS.register("tank", () -> new BlockItem(MULTITANK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<MultiTankTileEntity>> MULTITANK_TILE = TILES.register("tank", () -> TileEntityType.Builder.create(MultiTankTileEntity::new, MULTITANK.get()).build(null));

    public static final RegistryObject<BaseBlock> NODE = BLOCKS.register("node", NodeBlock::new);
    public static final RegistryObject<Item> NODE_ITEM = ITEMS.register("node", () -> new BlockItem(NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<NodeTileEntity>> NODE_TILE = TILES.register("node", () -> TileEntityType.Builder.create(NodeTileEntity::new, NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> PROCESSOR = BLOCKS.register("processor", NodeBlock::new);
    public static final RegistryObject<Item> PROCESSOR_ITEM = ITEMS.register("processor", () -> new BlockItem(PROCESSOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProcessorTileEntity>> PROCESSOR_TILE = TILES.register("processor", () -> TileEntityType.Builder.create(ProcessorTileEntity::new, PROCESSOR.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> PROGRAMMER = BLOCKS.register("programmer", NodeBlock::new);
    public static final RegistryObject<Item> PROGRAMMER_ITEM = ITEMS.register("programmer", () -> new BlockItem(PROGRAMMER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProgrammerTileEntity>> PROGRAMMER_TILE = TILES.register("programmer", () -> TileEntityType.Builder.create(ProgrammerTileEntity::new, PROGRAMMER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> PROGRAMMER_CONTAINER = CONTAINERS.register("programmer", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> WORKBENCH = BLOCKS.register("workbench", NodeBlock::new);
    public static final RegistryObject<Item> WORKBENCH_ITEM = ITEMS.register("workbench", () -> new BlockItem(WORKBENCH.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProgrammerTileEntity>> WORKBENCH_TILE = TILES.register("workbench", () -> TileEntityType.Builder.create(ProgrammerTileEntity::new, WORKBENCH.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> WORKBENCH_CONTAINER = CONTAINERS.register("workbench", GenericContainer::createContainerType);

    public static Item.Properties createStandardProperties() {
        return new Item.Properties().group(RFToolsControl.setup.getTab());
    }
}
