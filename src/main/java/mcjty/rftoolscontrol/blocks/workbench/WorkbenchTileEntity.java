package mcjty.rftoolscontrol.blocks.workbench;


import mcjty.lib.container.GenericCrafter;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.FacedSidedInvWrapper;
import mcjty.lib.varia.NullSidedInvWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;

public class WorkbenchTileEntity extends GenericTileEntity implements DefaultSidedInventory, GenericCrafter {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, WorkbenchContainer.factory, WorkbenchContainer.BUFFER_SIZE + 10);

    // This field contains the number of real items in the craft output slot. i.e. these are
    // items that are already crafted but only (partially) consumed.
    private int realItems = 0;

    private static final int[] DOWN_SLOTS = new int[]{WorkbenchContainer.SLOT_CRAFTOUTPUT};
    private static final int[] UP_SLOTS;
    private static final int[] SIDE_SLOTS;
    private static final int[] ALL_SLOTS;

    static {
        UP_SLOTS = new int[9];
        for (int i = 0 ; i < 9 ; i++) {
            UP_SLOTS[i] = i + WorkbenchContainer.SLOT_CRAFTINPUT;
        }
        SIDE_SLOTS = new int[WorkbenchContainer.BUFFER_SIZE];
        for (int i = 0 ; i < WorkbenchContainer.BUFFER_SIZE ; i++) {
            SIDE_SLOTS[i] = i + WorkbenchContainer.SLOT_BUFFER;
        }
        ALL_SLOTS = new int[WorkbenchContainer.BUFFER_SIZE + 9];
        for (int i = 0 ; i < 9 ; i++) {
            ALL_SLOTS[i] = i + WorkbenchContainer.SLOT_CRAFTINPUT;
        }
        for (int i = 0 ; i < WorkbenchContainer.BUFFER_SIZE ; i++) {
            ALL_SLOTS[i+9] = i + WorkbenchContainer.SLOT_BUFFER;
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }


    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        realItems = tagCompound.getInteger("realItems");
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("realItems", realItems);
    }

    private boolean isCraftInputSlot(int slot) {
        return slot >= WorkbenchContainer.SLOT_CRAFTINPUT && slot < WorkbenchContainer.SLOT_CRAFTOUTPUT;
    }

    private boolean isBufferSlot(int slot) {
        return slot >= WorkbenchContainer.SLOT_BUFFER && slot < WorkbenchContainer.SLOT_BUFFER + WorkbenchContainer.BUFFER_SIZE;
    }

    private boolean isCraftOutput(int slot) {
        return slot == WorkbenchContainer.SLOT_CRAFTOUTPUT;
    }

    private void updateRecipe() {
        if (getStackInSlot(WorkbenchContainer.SLOT_CRAFTOUTPUT) == null || realItems == 0) {
            InventoryCrafting workInventory = makeWorkInventory();
            IRecipe recipe = CraftingManager.findMatchingRecipe(workInventory, this.getWorld());
            if (recipe != null) {
                ItemStack stack = recipe.getCraftingResult(workInventory);
                getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), WorkbenchContainer.SLOT_CRAFTOUTPUT, stack);
            } else {
                getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), WorkbenchContainer.SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
            }
        }
    }

    private InventoryCrafting makeWorkInventory() {
        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);
        for (int i = 0 ; i < 9 ; i++) {
            workInventory.setInventorySlotContents(i, getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT));
        }
        return workInventory;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (isCraftInputSlot(index)) {
            updateRecipe();
        }
    }

    @Override
    public void craftItem() {
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
        if (direction == null) {
            return !isCraftOutput(index);
        } else if (direction == Direction.DOWN) {
            return false;
        } else if (direction == Direction.UP) {
            return isCraftInputSlot(index);
        } else {
            return isBufferSlot(index);
        }
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        if (direction == null) {
            return true;
        } else if (direction == Direction.DOWN) {
            return isCraftOutput(index);
        } else if (direction == Direction.UP) {
            return isCraftInputSlot(index);
        } else {
            return isBufferSlot(index);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == null) {
            return ALL_SLOTS;
        } else if (side == Direction.DOWN) {
            return DOWN_SLOTS;
        } else if (side == Direction.UP) {
            return UP_SLOTS;
        } else {
            return SIDE_SLOTS;
        }
    }



    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (isCraftOutput(index) && realItems == 0) {
            InventoryCrafting workInventory = makeWorkInventory();
            List<ItemStack> remainingItems = CraftingManager.getRemainingItems(workInventory, getWorld());
            for (int i = 0 ; i < 9 ; i++) {
                ItemStack s = getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT);
                if (!s.isEmpty()) {
                    getInventoryHelper().decrStackSize(i + WorkbenchContainer.SLOT_CRAFTINPUT, 1);
                    s = getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT);
                }

                if (!remainingItems.get(i).isEmpty()) {
                    if (s.isEmpty()) {
                        getInventoryHelper().setStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT, remainingItems.get(i));
                    } else if (ItemStack.areItemsEqual(s, remainingItems.get(i)) && ItemStack.areItemStackTagsEqual(s, remainingItems.get(i))) {
                        ItemStack stack = remainingItems.get(i);
                        stack.grow(s.getCount());
                        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), i + WorkbenchContainer.SLOT_CRAFTINPUT, remainingItems.get(i));
                    } else {
                        // @todo
                        // Not enough room!
                    }
                }
            }
        }
        ItemStack rc = getInventoryHelper().decrStackSize(index, count);
        if (isCraftOutput(index)) {
            ItemStack stack = getStackInSlot(index);
            if (!stack.isEmpty()) {
                realItems = stack.getCount();
            } else {
                realItems = 0;
            }
        }
        if (isCraftInputSlot(index) || isCraftOutput(index)) {
            updateRecipe();
        }
        return rc;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    protected FacedSidedInvWrapper[] handler = new FacedSidedInvWrapper[] { null, null, null, null, null, null };

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                if (invHandlerNull == null) {
                    invHandlerNull = new NullSidedInvWrapper(this);
                }
                return (T) invHandlerNull;
            } else {
                if (handler[facing.ordinal()] == null) {
                    handler[facing.ordinal()] = new FacedSidedInvWrapper(this, facing);
                }
                return (T) handler[facing.ordinal()];
            }
        }
        return super.getCapability(capability, facing);
    }
}
