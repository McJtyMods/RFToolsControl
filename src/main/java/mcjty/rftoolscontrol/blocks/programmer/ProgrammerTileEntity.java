package mcjty.rftoolscontrol.blocks.programmer;


import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class ProgrammerTileEntity extends GenericTileEntity {

    private NoDirectionItemHander items = createItemHandler();
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ProgrammerContainer>("Programmer")
            .containerSupplier((windowId,player) -> new ProgrammerContainer(windowId, ProgrammerContainer.CONTAINER_FACTORY, getPos(), ProgrammerTileEntity.this))
            .itemHandler(itemHandler));

    public ProgrammerTileEntity() {
        super(Registration.PROGRAMMER_TILE.get());
        items.setStackInSlot(ProgrammerContainer.SLOT_DUMMY, new ItemStack(Registration.PROGRAM_CARD.get()));
    }

    public NoDirectionItemHander getItems() {
        return items;
    }

    @Override
    public void setPowerInput(int powered) {
        int p = powerLevel;
        super.setPowerInput(powered);
        if (p != powerLevel && powerLevel > 0) {
            // Copy program in programmer to card
            ItemStack dummy = items.getStackInSlot(ProgrammerContainer.SLOT_DUMMY);
            if (dummy.isEmpty()) {
                return;
            }
            if (!dummy.hasTag()) {
                return;
            }
            ItemStack card = items.getStackInSlot(ProgrammerContainer.SLOT_CARD);
            if (card.isEmpty()) {
                return;
            }
            ProgramCardInstance instance = ProgramCardInstance.parseInstance(dummy);
            if (instance == null) {
                return;
            }
            ProgramCardItem.setCardName(card, ProgramCardItem.getCardName(dummy));
            INBT newGrid = dummy.getTag().get("grid").copy();
            card.getTag().put("grid", newGrid);
        }
    }

    // @todo 1.15 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
//        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    // @todo 1.15 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
//        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ProgrammerTileEntity.this, ProgrammerContainer.CONTAINER_FACTORY) {

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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationItemHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}

