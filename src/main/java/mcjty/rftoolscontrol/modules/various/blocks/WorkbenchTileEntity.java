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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
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
    private Recipe findRecipe(CraftingInput workInventory) {
        RecipeManager manager = level.getRecipeManager();
        for (RecipeHolder rh : manager.getRecipes()) {
            Recipe r = rh.value();
            if (r != null && RecipeType.CRAFTING.equals(r.getType()) && r.matches(workInventory, level)) {
                return r;
            }
        }
        return null;
    }

    private void updateRecipe() {
        if (items.getStackInSlot(SLOT_CRAFTOUTPUT).isEmpty() || getRealItems() == 0) {
            CraftingInput workInventory = makeWorkInventory();
            Recipe recipe = findRecipe(workInventory);
            if (recipe != null) {
                ItemStack stack = BaseRecipe.assemble(recipe, workInventory, level);
                items.setStackInSlot(SLOT_CRAFTOUTPUT, stack);
                setRealItems(stack.isEmpty() ? 0 : stack.getCount());
            } else {
                items.setStackInSlot(SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
                setRealItems(0);
            }
        }
    }

    private static List<ItemStack> createList() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0 ; i < 9 ; i++) {
            list.add(ItemStack.EMPTY);
        }
        return list;
    }

    private CraftingInput makeWorkInventory() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(i, items.get(i + SLOT_CRAFTINPUT));
        }
        return CraftingInput.of(3, 3, items);
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
                    if (isCraftOutput(slot) && getRealItems() == 0) {
                        CraftingInput workInventory = makeWorkInventory();
                        Recipe recipe = findRecipe(workInventory);
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
                                    } else if (ItemStack.isSameItem(s, remainingItems.get(i)) && ItemStack.isSameItemSameComponents(s, remainingItems.get(i))) {
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
