package mcjty.rftoolscontrol.blocks.workbench;


import mcjty.lib.McJtyLib;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.GenericCrafter;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.FacedSidedInvWrapper;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorkbenchTileEntity extends GenericTileEntity implements GenericCrafter {

    private NoDirectionItemHander items = createItemHandler();
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    // This field contains the number of real items in the craft output slot. i.e. these are
    // items that are already crafted but only (partially) consumed.
    private int realItems = 0;

    private static final int[] DOWN_SLOTS = new int[]{WorkbenchContainer.SLOT_CRAFTOUTPUT};
    private static final int[] UP_SLOTS;
    private static final int[] SIDE_SLOTS;
    private static final int[] ALL_SLOTS;

    static {
        UP_SLOTS = new int[9];
        for (int i = 0; i < 9; i++) {
            UP_SLOTS[i] = i + WorkbenchContainer.SLOT_CRAFTINPUT;
        }
        SIDE_SLOTS = new int[WorkbenchContainer.BUFFER_SIZE];
        for (int i = 0; i < WorkbenchContainer.BUFFER_SIZE; i++) {
            SIDE_SLOTS[i] = i + WorkbenchContainer.SLOT_BUFFER;
        }
        ALL_SLOTS = new int[WorkbenchContainer.BUFFER_SIZE + 9];
        for (int i = 0; i < 9; i++) {
            ALL_SLOTS[i] = i + WorkbenchContainer.SLOT_CRAFTINPUT;
        }
        for (int i = 0; i < WorkbenchContainer.BUFFER_SIZE; i++) {
            ALL_SLOTS[i + 9] = i + WorkbenchContainer.SLOT_BUFFER;
        }
    }

    public WorkbenchTileEntity() {
        super(Registration.WORKBENCH_TILE.get());
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }


    // @todo 1.15 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
//        readBufferFromNBT(tagCompound, inventoryHelper);
        realItems = tagCompound.getInt("realItems");
    }

    // @todo 1.15 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
//        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.putInt("realItems", realItems);
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

    @Nullable
    private IRecipe findRecipe(CraftingInventory workInventory) {
        for (IRecipe r : McJtyLib.proxy.getRecipeManager(world).getRecipes()) {
            if (r != null && IRecipeType.CRAFTING.equals(r.getType()) && r.matches(workInventory, world)) {
                return r;
            }
        }
        return null;
    }

    private void updateRecipe() {
        if (items.getStackInSlot(WorkbenchContainer.SLOT_CRAFTOUTPUT) == null || realItems == 0) {
            CraftingInventory workInventory = makeWorkInventory();
            IRecipe recipe = findRecipe(workInventory);
            if (recipe != null) {
                ItemStack stack = recipe.getCraftingResult(workInventory);
                items.setStackInSlot(WorkbenchContainer.SLOT_CRAFTOUTPUT, stack);
            } else {
                items.setStackInSlot(WorkbenchContainer.SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
            }
        }
    }

    private CraftingInventory makeWorkInventory() {
        CraftingInventory workInventory = new CraftingInventory(new Container(null, -1) {
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            workInventory.setInventorySlotContents(i, items.getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT));
        }
        return workInventory;
    }

    // @todo 1.15
//    @Override
//    public void setInventorySlotContents(int index, ItemStack stack) {
//        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
//        if (isCraftInputSlot(index)) {
//            updateRecipe();
//        }
//    }

    @Override
    public void craftItem() {
    }

    // @todo 1.15
//    @Override
//    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
//        if (direction == null) {
//            return !isCraftOutput(index);
//        } else if (direction == Direction.DOWN) {
//            return false;
//        } else if (direction == Direction.UP) {
//            return isCraftInputSlot(index);
//        } else {
//            return isBufferSlot(index);
//        }
//    }

    // @todo 1.15
//    @Override
//    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
//        if (direction == null) {
//            return true;
//        } else if (direction == Direction.DOWN) {
//            return isCraftOutput(index);
//        } else if (direction == Direction.UP) {
//            return isCraftInputSlot(index);
//        } else {
//            return isBufferSlot(index);
//        }
//    }

    // @todo 1.15
//    @Override
//    public int[] getSlotsForFace(Direction side) {
//        if (side == null) {
//            return ALL_SLOTS;
//        } else if (side == Direction.DOWN) {
//            return DOWN_SLOTS;
//        } else if (side == Direction.UP) {
//            return UP_SLOTS;
//        } else {
//            return SIDE_SLOTS;
//        }
//    }


// @todo 1.15
//    @Override
//    public ItemStack decrStackSize(int index, int count) {
//        if (isCraftOutput(index) && realItems == 0) {
//            CraftingInventory workInventory = makeWorkInventory();
//            List<ItemStack> remainingItems = CraftingManager.getRemainingItems(workInventory, getWorld());
//            for (int i = 0 ; i < 9 ; i++) {
//                ItemStack s = getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT);
//                if (!s.isEmpty()) {
//                    getInventoryHelper().decrStackSize(i + WorkbenchContainer.SLOT_CRAFTINPUT, 1);
//                    s = getStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT);
//                }
//
//                if (!remainingItems.get(i).isEmpty()) {
//                    if (s.isEmpty()) {
//                        getInventoryHelper().setStackInSlot(i + WorkbenchContainer.SLOT_CRAFTINPUT, remainingItems.get(i));
//                    } else if (ItemStack.areItemsEqual(s, remainingItems.get(i)) && ItemStack.areItemStackTagsEqual(s, remainingItems.get(i))) {
//                        ItemStack stack = remainingItems.get(i);
//                        stack.grow(s.getCount());
//                        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), i + WorkbenchContainer.SLOT_CRAFTINPUT, remainingItems.get(i));
//                    } else {
//                        // @todo
//                        // Not enough room!
//                    }
//                }
//            }
//        }
//        ItemStack rc = getInventoryHelper().decrStackSize(index, count);
//        if (isCraftOutput(index)) {
//            ItemStack stack = getStackInSlot(index);
//            if (!stack.isEmpty()) {
//                realItems = stack.getCount();
//            } else {
//                realItems = 0;
//            }
//        }
//        if (isCraftInputSlot(index) || isCraftOutput(index)) {
//            updateRecipe();
//        }
//        return rc;
//    }

    protected FacedSidedInvWrapper[] handler = new FacedSidedInvWrapper[]{null, null, null, null, null, null};

    // @todo 1.15
//    @Override
//    public <T> T getCapabilixty(Capability<T> capability, Direction facing) {
//        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//            if (facing == null) {
//                if (invHandlerNull == null) {
//                    invHandlerNull = new NullSidedInvWrapper(this);
//                }
//                return (T) invHandlerNull;
//            } else {
//                if (handler[facing.ordinal()] == null) {
//                    handler[facing.ordinal()] = new FacedSidedInvWrapper(this, facing);
//                }
//                return (T) handler[facing.ordinal()];
//            }
//        }
//        return super.getCapability(capability, facing);
//    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(WorkbenchTileEntity.this, WorkbenchContainer.CONTAINER_FACTORY) {

            @Override
            protected void onUpdate(int index) {
                // @todo 1.15
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                // @todo 1.15
                return true;
            }
        };
    }

}