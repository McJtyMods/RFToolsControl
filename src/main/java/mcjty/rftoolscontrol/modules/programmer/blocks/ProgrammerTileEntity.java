package mcjty.rftoolscontrol.modules.programmer.blocks;


import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
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
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.rftoolscontrol.modules.programmer.ProgrammerModule.PROGRAMMER_CONTAINER;


public class ProgrammerTileEntity extends GenericTileEntity {

    public static final int SLOT_CARD = 0;
    public static final int SLOT_DUMMY = 1;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .box(specific(new ItemStack(VariousModule.PROGRAM_CARD.get())).in().out(), SLOT_CARD, 91, 136, 1, 1)
            .box(generic(), SLOT_DUMMY, -1000, -1000, 1, 1)
            .playerSlots(91, 157));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.basic(this, CONTAINER_FACTORY);

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Programmer")
            .containerSupplier(container(PROGRAMMER_CONTAINER, CONTAINER_FACTORY, this))
            .itemHandler(() -> items)
            .setupSync(this));

    public ProgrammerTileEntity() {
        super(ProgrammerModule.PROGRAMMER_TILE.get());
        items.setStackInSlot(SLOT_DUMMY, new ItemStack(VariousModule.PROGRAM_CARD.get()));
    }

    public GenericItemHandler getItems() {
        return items;
    }

    @Override
    public void setPowerInput(int powered) {
        int p = powerLevel;
        super.setPowerInput(powered);
        if (p != powerLevel && powerLevel > 0) {
            // Copy program in programmer to card
            ItemStack dummy = items.getStackInSlot(SLOT_DUMMY);
            if (dummy.isEmpty()) {
                return;
            }
            if (!dummy.hasTag()) {
                return;
            }
            ItemStack card = items.getStackInSlot(SLOT_CARD);
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

}

