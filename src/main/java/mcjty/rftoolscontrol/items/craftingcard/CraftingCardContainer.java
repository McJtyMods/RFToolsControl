package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.*;
import mcjty.lib.tools.ItemStackList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.List;

public class CraftingCardContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_INPUT = 0;
	public static final int GRID_WIDTH = 5;
	public static final int GRID_HEIGHT = 4;
	public static final int INPUT_SLOTS = GRID_WIDTH * GRID_HEIGHT;
	public static final int SLOT_OUT = INPUT_SLOTS;

	private int cardIndex;

	public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            layoutPlayerInventorySlots(10, 116);
        }
    };

    public CraftingCardContainer(EntityPlayer player) {
        super(factory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
		cardIndex = player.inventory.currentItem;
        generateSlots();
    }

	@Override
	protected Slot createSlot(SlotFactory slotFactory, IInventory inventory, int index, int x, int y, SlotType slotType) {
		if (slotType == SlotType.SLOT_PLAYERHOTBAR && index == cardIndex) {
			return new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
				@Override
				public boolean canTakeStack(EntityPlayer player) {
					// We don't want to take the stack from this slot.
					return false;
				}
			};
		} else {
			return super.createSlot(slotFactory, inventory, index, x, y, slotType);
		}
	}

	public void setGridContents(EntityPlayer player, List<ItemStack> stacks) {
		ItemStack craftingCard = player.getHeldItem(EnumHand.MAIN_HAND);
		ItemStackList s = ItemStackList.create(INPUT_SLOTS+1);
		int x = 0;
		int y = 0;
		for (int i = 0 ; i < stacks.size() ; i++) {
			if (i == 0) {
				s.set(SLOT_OUT, stacks.get(i));
			} else {
				int slot = y*GRID_WIDTH + x;
				s.set(slot, stacks.get(i));
				x++;
				if (x >= 3) { x = 0; y++; }
			}
		}
		CraftingCardItem.putStacksInItem(craftingCard, s);
	}
}
