package mcjty.rftoolscontrol.modules.processor.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;

public class ProcessorContainer extends GenericContainer {

    public static final int SLOT_EXPANSION = 0;     // 4*4 slots
    public static final int SLOT_CARD = EXPANSION_SLOTS;        // 6 slots
    public static final int SLOT_BUFFER = EXPANSION_SLOTS + CARD_SLOTS;  // 3*8 slots
    public static final int SLOTS = EXPANSION_SLOTS + CARD_SLOTS + ITEM_SLOTS;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(SLOTS)
            .box(generic(), SLOT_EXPANSION, 10, 157, 4, 4)
            .box(specific(new ItemStack(VariousModule.PROGRAM_CARD.get())).in().out(), SLOT_CARD, 10, 14, CARD_SLOTS, 1)
            .box(generic().in().out(), SLOT_BUFFER, 199, 7, 3, 8)
            .playerSlots(91, 157));

    private ProcessorContainer(MenuType<ProcessorContainer> type, int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
        super(type, id, CONTAINER_FACTORY.get(), pos, te, player);
    }

    public static ProcessorContainer create(int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
        return new ProcessorContainer(ProcessorModule.PROCESSOR_CONTAINER.get(), id, pos, te, player);
    }

    public static ProcessorContainer createRemote(int id, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
        return new ProcessorContainer(ProcessorModule.PROCESSOR_CONTAINER_REMOTE.get(), id, pos, te, player) {
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
    public boolean stillValid(@Nonnull Player player) {
        // If we are a remote container our canInteractWith should ignore distance
        if (isRemoteContainer()) {
            return te == null || !te.isRemoved();
        } else {
            return super.stillValid(player);
        }
    }


    @Override
    public void setupInventories(IItemHandler itemHandler, Inventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots(inventory.player);
    }
}
