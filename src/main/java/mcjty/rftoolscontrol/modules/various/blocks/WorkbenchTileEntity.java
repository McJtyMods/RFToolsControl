package mcjty.rftoolscontrol.modules.various.blocks;


import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.container.SlotDefinition.generic;

public class WorkbenchTileEntity extends GenericTileEntity {

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = 9*3;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(BUFFER_SIZE + 10)
            .box(generic(), SLOT_CRAFTINPUT, 42, 27, 3, 3)
            .box(SlotDefinition.craftResult().onCraft((tileEntity, playerEntity, stack) -> ((WorkbenchTileEntity)tileEntity).craftItem()), SLOT_CRAFTOUTPUT, 114, 45, 1, 1)
            .box(generic(), SLOT_BUFFER, 6, 99, 9, 3)
            .playerSlots(6, 157));

    private final GenericItemHandler items = createItemHandler();
    private final LazyOptional<WorkbenchItemHandler> automationItemHandlerUp = LazyOptional.of(() -> new WorkbenchItemHandler(items, Direction.UP));
    private final LazyOptional<WorkbenchItemHandler> automationItemHandlerDown = LazyOptional.of(() -> new WorkbenchItemHandler(items, Direction.DOWN));
    private final LazyOptional<WorkbenchItemHandler> automationItemHandlerSide = LazyOptional.of(() -> new WorkbenchItemHandler(items, null));

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<WorkbenchContainer>("Workbench")
            .containerSupplier((windowId, player) -> new WorkbenchContainer(windowId, CONTAINER_FACTORY.get(), getBlockPos(), WorkbenchTileEntity.this, player))
            .itemHandler(() -> items));

    // This field contains the number of real items in the craft output slot. i.e. these are
    // items that are already crafted but only (partially) consumed.
    private int realItems = 0;

    public WorkbenchTileEntity() {
        super(VariousModule.WORKBENCH_TILE.get());
    }


    @Override
    protected void loadInfo(CompoundNBT tagCompound) {
        super.loadInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        realItems = info.getInt("realItems");
    }

    @Override
    protected void saveInfo(CompoundNBT tagCompound) {
        super.saveInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("realItems", realItems);
    }

    private boolean isCraftInputSlot(int slot) {
        return slot >= SLOT_CRAFTINPUT && slot < SLOT_CRAFTOUTPUT;
    }

    private boolean isBufferSlot(int slot) {
        return slot >= SLOT_BUFFER && slot < SLOT_BUFFER + BUFFER_SIZE;
    }

    private boolean isCraftOutput(int slot) {
        return slot == SLOT_CRAFTOUTPUT;
    }

    @Nullable
    private IRecipe findRecipe(CraftingInventory workInventory) {
        RecipeManager manager = level.isClientSide() ? SafeClientTools.getRecipeManager(level) : level.getServer().getRecipeManager();
        for (IRecipe r : manager.getRecipes()) {
            if (r != null && IRecipeType.CRAFTING.equals(r.getType()) && r.matches(workInventory, level)) {
                return r;
            }
        }
        return null;
    }

    private void updateRecipe() {
        if (items.getStackInSlot(SLOT_CRAFTOUTPUT).isEmpty() || realItems == 0) {
            CraftingInventory workInventory = makeWorkInventory();
            IRecipe recipe = findRecipe(workInventory);
            if (recipe != null) {
                ItemStack stack = recipe.assemble(workInventory);
                items.setStackInSlot(SLOT_CRAFTOUTPUT, stack);
            } else {
                items.setStackInSlot(SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
            }
        }
    }

    private CraftingInventory makeWorkInventory() {
        CraftingInventory workInventory = new CraftingInventory(new Container(null, -1) {
            @SuppressWarnings("NullableProblems")
            @Override
            public boolean stillValid(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            workInventory.setItem(i, items.getStackInSlot(i + SLOT_CRAFTINPUT));
        }
        return workInventory;
    }

    public void craftItem() {
    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(WorkbenchTileEntity.this, CONTAINER_FACTORY.get()) {

            // While crafting we don't update the recipe
            private int crafting = 0;

            @Override
            protected void onUpdate(int index, ItemStack stack) {
                if (isCraftInputSlot(index)) {
                    if (crafting <= 0) {
                        updateRecipe();
                    }
                }
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (simulate) {
                    // Use the normal simulated item extraction
                    return super.extractItem(slot, amount, simulate);
                } else {
                    if (isCraftOutput(slot) && realItems == 0) {
                        CraftingInventory workInventory = makeWorkInventory();
                        IRecipe recipe = findRecipe(workInventory);
                        if (recipe != null) {
                            crafting++;
                            List<ItemStack> remainingItems = recipe.getRemainingItems(workInventory);
                            for (int i = 0; i < 9; i++) {
                                ItemStack s = items.getStackInSlot(i + SLOT_CRAFTINPUT);
                                if (!s.isEmpty()) {
                                    super.extractItem(i + SLOT_CRAFTINPUT, 1, false);
                                    s = items.getStackInSlot(i + SLOT_CRAFTINPUT);
                                }

                                if (!remainingItems.get(i).isEmpty()) {
                                    if (s.isEmpty()) {
                                        items.setStackInSlot(i + SLOT_CRAFTINPUT, remainingItems.get(i));
                                    } else if (ItemStack.isSame(s, remainingItems.get(i)) && ItemStack.tagMatches(s, remainingItems.get(i))) {
                                        ItemStack stack = remainingItems.get(i);
                                        stack.grow(s.getCount());
                                        items.setStackInSlot(i + SLOT_CRAFTINPUT, remainingItems.get(i));
                                    } else {
                                        // @todo
                                        // Not enough room!
                                    }
                                }
                            }
                            crafting--;
                        }
                    }
                    ItemStack rc = super.extractItem(slot, amount, false);
                    if (isCraftOutput(slot)) {
                        ItemStack stack = items.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            realItems = stack.getCount();
                        } else {
                            realItems = 0;
                        }
                    }
                    if (isCraftInputSlot(slot) || isCraftOutput(slot)) {
                        updateRecipe();
                    }
                    return rc;
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                // @todo 1.15
                return true;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.DOWN) {
                return automationItemHandlerDown.cast();
            } else if (facing == Direction.UP) {
                return automationItemHandlerUp.cast();
            }
            return automationItemHandlerSide.cast();
        }
        return super.getCapability(cap, facing);
    }

    public class WorkbenchItemHandler extends AutomationFilterItemHander {

        private final Direction direction;

        public WorkbenchItemHandler(GenericItemHandler wrapped, @Nullable Direction direction) {
            super(wrapped);
            this.direction = direction;
        }

        @Override
        public boolean canAutomationInsert(int index) {
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
        public boolean canAutomationExtract(int index) {
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


    }
}