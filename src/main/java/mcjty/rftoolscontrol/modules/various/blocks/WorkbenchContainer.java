package mcjty.rftoolscontrol.modules.various.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.generic;

public class WorkbenchContainer extends GenericContainer {

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = 9*3;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(BUFFER_SIZE + 10)
            .box(generic(), SLOT_CRAFTINPUT, 42, 27, 3, 3)
            .box(SlotDefinition.craftResult().onCraft((tileEntity, playerEntity, stack) -> ((WorkbenchTileEntity)tileEntity).craftItem()), SLOT_CRAFTOUTPUT, 114, 45, 1, 1)
            .box(generic(), SLOT_BUFFER, 6, 99, 9, 3)
            .playerSlots(6, 157));

//    @Override
//    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
//        if (index == SLOT_CRAFTOUTPUT) {
//            Slot slot = this.inventorySlots.get(index);
//            if (slot != null && slot.getHasStack()) {
//                ItemStack origStack = slot.getStack();
//                ItemStack copy = origStack.copy();
//                if (!this.mergeItemStacks(origStack, index, SlotType.SLOT_SPECIFICITEM, false) && !this.mergeItemStacks(origStack, index, SlotType.SLOT_PLAYERINV, true) && !this.mergeItemStacks(origStack, index, SlotType.SLOT_PLAYERHOTBAR, false)) {
//                    return null;
//                }
//
//                slot.onSlotChange(origStack, copy);
//            }
//        }
//        return super.transferStackInSlot(player, index);
//    }

    public WorkbenchContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(VariousModule.WORKBENCH_CONTAINER.get(), id, factory, pos, te);
    }
}
