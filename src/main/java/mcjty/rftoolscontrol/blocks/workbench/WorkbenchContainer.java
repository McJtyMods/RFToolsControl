package mcjty.rftoolscontrol.blocks.workbench;

import mcjty.lib.container.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

public class WorkbenchContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = 9*3;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_CRAFTINPUT, 42, 27, 3, 18, 3, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CRAFTRESULT), CONTAINER_INVENTORY, SLOT_CRAFTOUTPUT, 114, 45, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_BUFFER, 6, 99, 9, 18, 3, 18);
            layoutPlayerInventorySlots(6, 157);
        }
    };

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

    public WorkbenchContainer(PlayerEntity player, IInventory containerInventory) {
        super(factory);
        setCrafter((GenericCrafter) containerInventory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
