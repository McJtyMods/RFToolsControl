package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CraftingCardContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_INPUT = 0;
    public static final int INPUT_SLOTS = 5*4;
	public static final int SLOT_OUT = INPUT_SLOTS;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
			addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_INPUT, 10, 27, 5, 18, 4, 18);
			addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_OUT, 10 + 8*18, 27, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 106);
        }
    };

    public CraftingCardContainer(EntityPlayer player) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, new CraftingCardInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

	@Override
	protected Slot createSlot(SlotFactory slotFactory, IInventory inventory, int index, int x, int y, SlotType slotType) {
		if (slotType == SlotType.SLOT_GHOST) {
			return new GhostSlot(inventory, index, x, y) {

				@Override
				public int getSlotStackLimit() {
					return 0;
				}

				@Override
				public int getItemStackLimit(ItemStack stack) {
					return 0;
				}

				@Override
				public void putStack(ItemStack stack) {
					if (stack != null) {
						if (stack.stackSize == 0) {
							stack.stackSize = 1;
						}
					}
					inventory.setInventorySlotContents(getSlotIndex(), stack);
					onSlotChanged();
				}
			};
		} else {
			return super.createSlot(slotFactory, inventory, index, x, y, slotType);
		}
	}

	public void setGridContents(List<ItemStack> stacks) {
		int x = 0;
		int y = 0;
		for (int i = 0 ; i < stacks.size() ; i++) {
			if (i == 0) {
				this.inventorySlots.get(SLOT_OUT).putStack(stacks.get(i));
			} else {
				int slot = y*5 + x;
				this.inventorySlots.get(slot).putStack(stacks.get(i));
				x++;
				if (x >= 3) { x = 0; y++; }
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack() && index >= INPUT_SLOTS+1 && index < INPUT_SLOTS+1 + 36) {
			ItemStack itemstack1 = slot.getStack();
			ItemStack stack = itemstack1.copy();
			stack.stackSize = 1;
			IInventory inv = inventories.get(CONTAINER_INVENTORY);
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getStackInSlot(i) == null) {
					inv.setInventorySlotContents(i, stack);
					break;
				}
			}
			slot.onSlotChanged();

		}

		return null;
	}
}
