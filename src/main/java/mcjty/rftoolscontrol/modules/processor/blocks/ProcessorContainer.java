package mcjty.rftoolscontrol.modules.processor.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.container;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;

public class ProcessorContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_EXPANSION = 0;     // 4*4 slots
    public static final int SLOT_CARD = EXPANSION_SLOTS;        // 6 slots
    public static final int SLOT_BUFFER = EXPANSION_SLOTS + CARD_SLOTS;  // 3*8 slots
    public static final int SLOTS = EXPANSION_SLOTS + CARD_SLOTS + ITEM_SLOTS;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(SLOTS)
            .box(container(), CONTAINER_INVENTORY, SLOT_EXPANSION, 10, 157, 4, 4)
            .box(specific(new ItemStack(VariousModule.PROGRAM_CARD.get())), CONTAINER_INVENTORY, SLOT_CARD, 10, 14, CARD_SLOTS, 1)
            .box(container(), CONTAINER_INVENTORY, SLOT_BUFFER, 199, 7, 3, 8)
            .playerSlots(91, 157));

    private ProcessorContainer(ContainerType<ProcessorContainer> type, int id, BlockPos pos, @Nullable GenericTileEntity te) {
        super(type, id, CONTAINER_FACTORY.get(), pos, te);
    }

    public static ProcessorContainer create(int id, BlockPos pos, @Nullable GenericTileEntity te) {
        return new ProcessorContainer(ProcessorModule.PROCESSOR_CONTAINER.get(), id, pos, te);
    }

    public static ProcessorContainer createRemote(int id, BlockPos pos, @Nullable GenericTileEntity te) {
        return new ProcessorContainer(ProcessorModule.PROCESSOR_CONTAINER_REMOTE.get(), id, pos, te) {
            @Override
            protected boolean isRemoteContainer() {
                return true;
            }
        };
    }

    protected boolean isRemoteContainer() {
        return false;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        // If we are a remote container our canInteractWith should ignore distance
        if (isRemoteContainer()) {
            return te == null || !te.isRemoved();
        } else {
            return super.canInteractWith(player);
        }
    }


    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(CONTAINER_INVENTORY, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots(inventory.player);
    }
}
