package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ProgrammerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_CARD = 0;
    public static final int SLOT_DUMMY = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.programCardItem)), CONTAINER_INVENTORY, SLOT_CARD, 91, 136, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_DUMMY, -1000, -1000, 1, 18, 1, 18);
            layoutPlayerInventorySlots(91, 157);
        }
    };


    public ProgrammerContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
