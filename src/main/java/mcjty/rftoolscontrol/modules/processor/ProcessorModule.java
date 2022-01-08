package mcjty.rftoolscontrol.modules.processor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorBlock;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.client.ProcessorRenderer;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProcessorModule implements IModule {

    public static final RegistryObject<BaseBlock> PROCESSOR = BLOCKS.register("processor", ProcessorBlock::new);
    public static final RegistryObject<Item> PROCESSOR_ITEM = ITEMS.register("processor", () -> new BlockItem(PROCESSOR.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<ProcessorTileEntity>> PROCESSOR_TILE = TILES.register("processor", () -> BlockEntityType.Builder.of(ProcessorTileEntity::new, PROCESSOR.get()).build(null));
    public static final RegistryObject<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);
    public static final RegistryObject<MenuType<ProcessorContainer>> PROCESSOR_CONTAINER_REMOTE = CONTAINERS.register("processor_remote",
            () -> GenericContainer.createRemoteContainerType(ProcessorTileEntity::new, ProcessorContainer::createRemote, ProcessorContainer.SLOTS));

    public static final RegistryObject<CPUCoreItem> CPU_CORE_500 = ITEMS.register("cpu_core_500", () -> new CPUCoreItem(0));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_1000 = ITEMS.register("cpu_core_1000", () -> new CPUCoreItem(1));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_2000 = ITEMS.register("cpu_core_2000", () -> new CPUCoreItem(2));
    public static final RegistryObject<RAMChipItem> RAM_CHIP = ITEMS.register("ram_chip", RAMChipItem::new);
    public static final RegistryObject<NetworkCardItem> NETWORK_CARD = ITEMS.register("network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_NORMAL));
    public static final RegistryObject<NetworkCardItem> ADVANCED_NETWORK_CARD = ITEMS.register("advanced_network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_ADVANCED));
    public static final RegistryObject<NetworkIdentifierItem> NETWORK_IDENTIFIER = ITEMS.register("network_identifier", NetworkIdentifierItem::new);
    public static final RegistryObject<GraphicsCardItem> GRAPHICS_CARD = ITEMS.register("graphics_card", GraphicsCardItem::new);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiProcessor.register();
        });

        ProcessorRenderer.register();
    }

    @Override
    public void initConfig() {

    }
}
