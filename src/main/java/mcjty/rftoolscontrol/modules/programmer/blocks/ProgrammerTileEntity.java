package mcjty.rftoolscontrol.modules.programmer.blocks;


import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class ProgrammerTileEntity extends GenericTileEntity {

    private final NoDirectionItemHander items = createItemHandler();
    private final LazyOptional<AutomationFilterItemHander> itemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ProgrammerContainer>("Programmer")
            .containerSupplier((windowId,player) -> new ProgrammerContainer(windowId, ProgrammerContainer.CONTAINER_FACTORY.get(), getBlockPos(), ProgrammerTileEntity.this))
            .itemHandler(() -> items));

    public ProgrammerTileEntity() {
        super(ProgrammerModule.PROGRAMMER_TILE.get());
        items.setStackInSlot(ProgrammerContainer.SLOT_DUMMY, new ItemStack(VariousModule.PROGRAM_CARD.get()));
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

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ProgrammerTileEntity.this, ProgrammerContainer.CONTAINER_FACTORY.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}

