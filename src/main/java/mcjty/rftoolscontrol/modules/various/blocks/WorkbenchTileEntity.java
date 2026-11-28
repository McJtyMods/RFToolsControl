package mcjty.rftoolscontrol.modules.various.blocks;


import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.crafting.BaseRecipe;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.data.WorkbenchData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static mcjty.lib.container.SlotDefinition.generic;

public class WorkbenchTileEntity extends GenericTileEntity {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (IBlockCapabilityProvider<IItemHandler, Direction>) (level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof WorkbenchTileEntity workbench) {
                if (direction == Direction.DOWN) {
                    return workbench.automationItemHandlerDown;
                } else if (direction == Direction.UP) {
                    return workbench.automationItemHandlerUp;
                } else {
                    return workbench.automationItemHandlerSide;
                }
            }
            return null;
        }, VariousModule.WORKBENCH.block().get());
    }

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
    private final WorkbenchItemHandler automationItemHandlerUp = new WorkbenchItemHandler(items, Direction.UP);
    private final WorkbenchItemHandler automationItemHandlerDown = new WorkbenchItemHandler(items, Direction.DOWN);
    private final WorkbenchItemHandler automationItemHandlerSide = new WorkbenchItemHandler(items, null);

    @Cap(type = CapType.CONTAINER)
    private static final Function<WorkbenchTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<WorkbenchContainer>("Workbench")
            .containerSupplier((windowId, player) -> new WorkbenchContainer(windowId, CONTAINER_FACTORY.get(), tile.getBlockPos(), tile, player))
            .itemHandler(() -> tile.items);

    public WorkbenchTileEntity(BlockPos pos, BlockState state) {
        super(VariousModule.WORKBENCH.be().get(), pos, state);
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
    private CraftingRecipe findRecipe(CraftingInput workInventory) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, workInventory, level)
                .map(RecipeHolder::value).orElse(null);
    }

    private void updateRecipe() {
        if (items.getStackInSlot(SLOT_CRAFTOUTPUT).isEmpty() || getRealItems() == 0) {
            CraftingInput workInventory = makeWorkInventory().input();
            CraftingRecipe recipe = findRecipe(workInventory);
            setRealItems(0);
            items.setStackInSlot(SLOT_CRAFTOUTPUT, recipe == null
                    ? ItemStack.EMPTY : BaseRecipe.assemble(recipe, workInventory, level));
        }
    }

    private CraftingInput.Positioned makeWorkInventory() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(items.getStackInSlot(i + SLOT_CRAFTINPUT));
        }
        return CraftingInput.ofPositioned(3, 3, grid);
    }

    public void craftItem() {
    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(WorkbenchTileEntity.this, CONTAINER_FACTORY.get()) {

            // While crafting we don't update the recipe
            private int crafting = 0;

            @Override
            protected void onUpdate(int index, ItemStack stack) {
                super.onUpdate(index, stack);
                if (isCraftInputSlot(index) && crafting == 0) {
                    updateRecipe();
                }
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount <= 0 || getStackInSlot(slot).isEmpty()) {
                    return ItemStack.EMPTY;
                }
                if (simulate) {
                    // Use the normal simulated item extraction
                    return super.extractItem(slot, amount, simulate);
                } else {
                    if (isCraftOutput(slot) && getRealItems() == 0) {
                        CraftingInput.Positioned positioned = makeWorkInventory();
                        CraftingInput workInventory = positioned.input();
                        CraftingRecipe recipe = findRecipe(workInventory);
                        if (recipe == null) {
                            updateRecipe();
                            return ItemStack.EMPTY;
                        }

                        crafting++;
                        try {
                            List<ItemStack> remainingItems = recipe.getRemainingItems(workInventory);
                            for (int i = 0; i < workInventory.size(); i++) {
                                int x = positioned.left() + i % workInventory.width();
                                int y = positioned.top() + i / workInventory.width();
                                int inputSlot = SLOT_CRAFTINPUT + x + 3 * y;
                                ItemStack s = items.getStackInSlot(inputSlot);
                                if (!s.isEmpty()) {
                                    super.extractItem(inputSlot, 1, false);
                                    s = items.getStackInSlot(inputSlot);
                                }

                                if (!remainingItems.get(i).isEmpty()) {
                                    if (s.isEmpty()) {
                                        items.setStackInSlot(inputSlot, remainingItems.get(i));
                                    } else if (ItemStack.isSameItem(s, remainingItems.get(i)) && ItemStack.isSameItemSameComponents(s, remainingItems.get(i))) {
                                        ItemStack stack = remainingItems.get(i);
                                        stack.grow(s.getCount());
                                        items.setStackInSlot(inputSlot, remainingItems.get(i));
                                    } else {
                                        // @todo
                                        // Not enough room!
                                    }
                                }
                            }
                        } finally {
                            crafting--;
                        }
                    }
                    ItemStack rc = super.extractItem(slot, amount, false);
                    if (isCraftOutput(slot)) {
                        ItemStack stack = items.getStackInSlot(slot);
                        setRealItems(stack.isEmpty() ? 0 : stack.getCount());
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        items.save(tag, "items", provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        items.load(tag, "items", provider);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(VariousModule.ITEM_WORKBENCH_DATA.get(), getWorkbenchData());
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        WorkbenchData data = input.get(VariousModule.ITEM_WORKBENCH_DATA.get());
        if (data != null) {
            setData(VariousModule.WORKBENCH_DATA.get(), data);
        }
    }

    private int getRealItems() {
        return getWorkbenchData().realItems();
    }

    private void setRealItems(int realItems) {
        WorkbenchData data = getWorkbenchData();
        if (data.realItems() != realItems) {
            setData(VariousModule.WORKBENCH_DATA.get(), data.withRealItems(realItems));
            setChanged();
        }
    }

    private WorkbenchData getWorkbenchData() {
        WorkbenchData data = getData(VariousModule.WORKBENCH_DATA.get());
        if (data == null) {
            data = WorkbenchData.createDefault();
            setData(VariousModule.WORKBENCH_DATA.get(), data);
        }
        return data;
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
