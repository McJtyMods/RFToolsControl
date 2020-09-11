package mcjty.rftoolscontrol.modules.programmer.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

import static mcjty.lib.container.SlotDefinition.container;
import static mcjty.lib.container.SlotDefinition.specific;

public class ProgrammerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_CARD = 0;
    public static final int SLOT_DUMMY = 1;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .box(specific(new ItemStack(VariousModule.PROGRAM_CARD.get())), CONTAINER_INVENTORY, SLOT_CARD, 91, 136, 1, 1)
            .box(container(), CONTAINER_INVENTORY, SLOT_DUMMY, -1000, -1000, 1, 1)
            .playerSlots(91, 157));

    public ProgrammerContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(ProgrammerModule.PROGRAMMER_CONTAINER.get(), id, factory, pos, te);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(CONTAINER_INVENTORY, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots(inventory.player);
    }
}
