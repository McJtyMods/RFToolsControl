package mcjty.rftoolscontrol.modules.various.blocks;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.crafting.BaseRecipe;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Lazy;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.data.WorkbenchData;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.setup.Registration.ITEM_INVENTORY;

public class WorkbenchTileEntity extends GenericTileEntity {

    public static void registerCapabilities() {
        ItemStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, direction) -> {
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
            .box(SlotDefinition.craftResult(), SLOT_CRAFTOUTPUT, 114, 45, 1, 1)
            .box(generic(), SLOT_BUFFER, 6, 99, 9, 3)
            .playerSlots(6, 157));

    private final WorkbenchInventory items = new WorkbenchInventory();
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
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, workInventory, level)
                    .map(RecipeHolder::value).orElse(null);
        }
        return null;
    }

    private void updateRecipe() {
        if (items.getStackInSlot(SLOT_CRAFTOUTPUT).isEmpty() || getRealItems() == 0) {
            CraftingInput workInventory = makeWorkInventory();
            CraftingRecipe recipe = findRecipe(workInventory);
            if (recipe != null) {
                ItemStack stack = BaseRecipe.assemble(recipe, workInventory, level);
                items.setStackInSlot(SLOT_CRAFTOUTPUT, stack);
            } else {
                items.setStackInSlot(SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
                setRealItems(0);
            }
        }
    }

    private CraftingInput makeWorkInventory() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(items.getStackInSlot(i + SLOT_CRAFTINPUT));
        }
        return CraftingInput.of(3, 3, grid);
    }

    private record InventorySnapshot(List<ItemStack> stacks, int realItems) {
    }

    private class WorkbenchInventory extends GenericItemHandler {

        private int crafting;
        private final SnapshotParticipant<InventorySnapshot> journal = new SnapshotParticipant<>() {

            @Override
            protected InventorySnapshot createSnapshot() {
                List<ItemStack> stacks = new ArrayList<>(getSlots());
                for (int i = 0; i < getSlots(); i++) {
                    stacks.add(getStackInSlot(i).copy());
                }
                return new InventorySnapshot(stacks, getRealItems());
            }

            @Override
            protected void readSnapshot(InventorySnapshot snapshot) {
                crafting++;
                try {
                    for (int i = 0; i < snapshot.stacks().size(); i++) {
                        setStackInSlot(i, snapshot.stacks().get(i));
                    }
                    setRealItems(snapshot.realItems());
                } finally {
                    crafting--;
                }
            }
        };

        private WorkbenchInventory() {
            super(WorkbenchTileEntity.this, CONTAINER_FACTORY.get());
        }

        @Override
        public long insert(int index, ItemVariant resource, long amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isBlank() || isCraftOutput(index)) {
                return 0;
            }
            ItemStack offered = resource.toStack((int) Math.min(amount, Integer.MAX_VALUE));
            int inserted = offered.getCount() - insertItem(index, offered, true).getCount();
            if (inserted > 0) {
                journal.updateSnapshots(transaction);
                insertItem(index, offered, false);
            }
            return inserted;
        }

        @Override
        public long extract(int index, ItemVariant resource, long amount, TransactionContext transaction) {
            if (amount <= 0 || resource.isBlank() || !resource.equals(getResource(index))) {
                return 0;
            }
            journal.updateSnapshots(transaction);
            return extractItem(index, (int) Math.min(amount, Integer.MAX_VALUE), false).getCount();
        }

        @Override
        protected void onUpdate(int index, ItemStack stack) {
            super.onUpdate(index, stack);
            if (isCraftInputSlot(index) && crafting == 0) {
                updateRecipe();
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            if (amount <= 0 || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int count = Math.min(amount, stack.getCount());
            ItemStack taken = stack.copyWithCount(count);
            if (isCraftOutput(slot) && getRealItems() == 0) {
                CraftingInput grid = makeWorkInventory();
                CraftingRecipe recipe = findRecipe(grid);
                if (recipe == null || !consumeIngredients(grid, recipe, simulate)) {
                    return ItemStack.EMPTY;
                }
            }

            if (simulate) {
                return taken;
            }

            ItemStack remaining = stack.copyWithCount(stack.getCount() - count);
            setStackInSlot(slot, remaining);
            if (isCraftOutput(slot)) {
                setRealItems(remaining.getCount());
                updateRecipe();
            }
            return taken;
        }

        private boolean consumeIngredients(CraftingInput grid, CraftingRecipe recipe, boolean simulate) {
            List<ItemStack> remainders = recipe.getRemainingItems(grid);
            List<ItemStack> updated = new ArrayList<>(getSlots());
            for (int i = 0; i < getSlots(); i++) {
                updated.add(getStackInSlot(i).copy());
            }
            for (int i = 0; i < grid.size(); i++) {
                int slot = i + SLOT_CRAFTINPUT;
                ItemStack remaining = updated.get(slot);
                remaining.shrink(1);
                ItemStack remainder = remainders.get(i).copy();
                if (remaining.isEmpty()) {
                    updated.set(slot, remainder);
                } else if (!remainder.isEmpty()) {
                    mergeRemainder(updated, slot, remainder);
                    for (int buffer = SLOT_BUFFER; buffer < SLOT_BUFFER + BUFFER_SIZE && !remainder.isEmpty(); buffer++) {
                        mergeRemainder(updated, buffer, remainder);
                    }
                    if (!remainder.isEmpty()) {
                        return false;
                    }
                }
            }

            if (!simulate) {
                crafting++;
                try {
                    for (int i = 0; i < updated.size(); i++) {
                        if (i != SLOT_CRAFTOUTPUT && !ItemStack.matches(getStackInSlot(i), updated.get(i))) {
                            setStackInSlot(i, updated.get(i));
                        }
                    }
                } finally {
                    crafting--;
                }
            }
            return true;
        }

        private void mergeRemainder(List<ItemStack> stacks, int slot, ItemStack remainder) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, remainder)) {
                int move = Math.min(remainder.getCount(), remainder.getMaxStackSize() - stack.getCount());
                if (move > 0) {
                    stacks.set(slot, remainder.copyWithCount(stack.getCount() + move));
                    remainder.shrink(move);
                }
            }
        }

    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("items", CompoundTag.CODEC).ifPresent(tag -> items.deserializeNBT(input.lookup(), tag));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("items", CompoundTag.CODEC, items.serializeNBT(level == null ? RegistryAccess.EMPTY : level.registryAccess()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        items.collectImplicitComponents(builder);
        builder.set(VariousModule.ITEM_WORKBENCH_DATA.get(), getWorkbenchData());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        items.applyImplicitComponents(input.get(ITEM_INVENTORY.get()));
        WorkbenchData data = input.get(VariousModule.ITEM_WORKBENCH_DATA.get());
        if (data != null) {
            setData(VariousModule.WORKBENCH_DATA, data);
        }
    }

    private int getRealItems() {
        return getWorkbenchData().realItems();
    }

    private void setRealItems(int realItems) {
        WorkbenchData data = getWorkbenchData();
        if (data.realItems() != realItems) {
            setData(VariousModule.WORKBENCH_DATA, data.withRealItems(realItems));
            setChanged();
        }
    }

    private WorkbenchData getWorkbenchData() {
        WorkbenchData data = getData(VariousModule.WORKBENCH_DATA);
        if (data == null) {
            data = WorkbenchData.createDefault();
            setData(VariousModule.WORKBENCH_DATA, data);
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
