package mcjty.rftoolscontrol.modules.processor.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;

public class ProcessorContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_EXPANSION = 0;     // 4*4 slots
    public static final int SLOT_CARD = EXPANSION_SLOTS;        // 6 slots
    public static final int SLOT_BUFFER = EXPANSION_SLOTS + CARD_SLOTS;  // 3*8 slots
    public static final int SLOTS = EXPANSION_SLOTS + CARD_SLOTS + ITEM_SLOTS;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(SLOTS) {
        @Override
        protected void setup() {
            box(SlotDefinition.container(),
                    CONTAINER_INVENTORY, SLOT_EXPANSION, 10, 157, 4, 4);
            box(SlotDefinition.specific(new ItemStack(VariousSetup.PROGRAM_CARD.get())),
                    CONTAINER_INVENTORY, SLOT_CARD, 10, 14, CARD_SLOTS, 1);
            box(SlotDefinition.container(),
                    CONTAINER_INVENTORY, SLOT_BUFFER, 199, 7, 3, 8);
            playerSlots(91, 157);
        }
    };

    public ProcessorContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(ProcessorSetup.PROCESSOR_CONTAINER.get(), id, factory, pos, te);
    }

    public ProcessorContainer(ContainerType<ProcessorContainer> type, int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(type, id, factory, pos, te);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(CONTAINER_INVENTORY, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots();
    }
}
