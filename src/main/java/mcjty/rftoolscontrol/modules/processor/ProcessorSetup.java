package mcjty.rftoolscontrol.modules.processor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorBlock;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.items.ItemStackHandler;

import static mcjty.rftoolscontrol.setup.Registration.*;

public class ProcessorSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<BaseBlock> PROCESSOR = BLOCKS.register("processor", ProcessorBlock::new);
    public static final RegistryObject<Item> PROCESSOR_ITEM = ITEMS.register("processor", () -> new BlockItem(PROCESSOR.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<ProcessorTileEntity>> PROCESSOR_TILE = TILES.register("processor", () -> TileEntityType.Builder.create(ProcessorTileEntity::new, PROCESSOR.get()).build(null));
    public static final RegistryObject<ContainerType<ProcessorContainer>> PROCESSOR_CONTAINER = CONTAINERS.register("processor", GenericContainer::createContainerType);
    public static final RegistryObject<ContainerType<ProcessorContainer>> PROCESSOR_CONTAINER_REMOTE = CONTAINERS.register("processor_remote", ProcessorSetup::createProcessorRemote);

    public static final RegistryObject<CPUCoreItem> CPU_CORE_500 = ITEMS.register("cpu_core_500", () -> new CPUCoreItem(0));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_1000 = ITEMS.register("cpu_core_1000", () -> new CPUCoreItem(1));
    public static final RegistryObject<CPUCoreItem> CPU_CORE_2000 = ITEMS.register("cpu_core_2000", () -> new CPUCoreItem(2));
    public static final RegistryObject<RAMChipItem> RAM_CHIP = ITEMS.register("ram_chip", RAMChipItem::new);
    public static final RegistryObject<NetworkCardItem> NETWORK_CARD = ITEMS.register("network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_NORMAL));
    public static final RegistryObject<NetworkCardItem> ADVANCED_NETWORK_CARD = ITEMS.register("advanced_network_card", () -> new NetworkCardItem(NetworkCardItem.TIER_ADVANCED));
    public static final RegistryObject<NetworkIdentifierItem> NETWORK_IDENTIFIER = ITEMS.register("network_identifier", NetworkIdentifierItem::new);
    public static final RegistryObject<GraphicsCardItem> GRAPHICS_CARD = ITEMS.register("graphics_card", GraphicsCardItem::new);

    public static ContainerType<ProcessorContainer> createProcessorRemote() {
        ContainerType<ProcessorContainer> containerType = IForgeContainerType.create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();

            ProcessorTileEntity te = new ProcessorTileEntity() {
                @Override
                public boolean isDummy() {
                    return true;
                }
            }; // Dummy tile entity
            te.setWorldAndPos(inv.player.getEntityWorld(), pos);    // @todo wrong world
            CompoundNBT compound = data.readCompoundTag();
            te.read(compound);

            ProcessorContainer container = new ProcessorContainer(PROCESSOR_CONTAINER_REMOTE.get(), windowId, ProcessorContainer.CONTAINER_FACTORY, pos, te);
            container.setupInventories(new ItemStackHandler(ProcessorContainer.SLOTS), inv);
            return container;
        });
        return containerType;
    }

}
