package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
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

	public void setGridContents(List<ItemStack> stacks) {
		int x = 0;
		int y = 0;
		for (int i = 0 ; i < stacks.size() ; i++) {
			if (i == 0) {
				this.inventorySlots.get(SLOT_OUT).putStack(stacks.get(i));
			} else {
				int slot = y*7 + x;
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
