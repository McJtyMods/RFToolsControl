package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ProcessorContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int CARD_SLOTS = 6;

    public static final int SLOT_EXPANSION = 0;     // 4*4 slots
    public static final int SLOT_CARD = 4*4;        // 6 slots
    public static final int SLOT_BUFFER = 4*4 + CARD_SLOTS;  // 3*8 slots
    public static final int SLOTS = 4*4+ CARD_SLOTS +3*8;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER),
                    CONTAINER_INVENTORY, SLOT_EXPANSION, 10, 157, 4, 18, 4, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.programCardItem)),
                    CONTAINER_INVENTORY, SLOT_CARD, 10, 14, CARD_SLOTS, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER),
                    CONTAINER_INVENTORY, SLOT_BUFFER, 199, 7, 3, 18, 8, 18);
            layoutPlayerInventorySlots(91, 157);
        }
    };


    public ProcessorContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
