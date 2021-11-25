package mcjty.rftoolscontrol.modules.programmer.blocks;


import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.LazyOptional;


public class ProgrammerTileEntity extends GenericTileEntity {

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final NoDirectionItemHander items = createItemHandler();

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ProgrammerContainer>("Programmer")
            .containerSupplier(windowId -> new ProgrammerContainer(windowId, ProgrammerContainer.CONTAINER_FACTORY.get(), getBlockPos(), ProgrammerTileEntity.this))
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
}

