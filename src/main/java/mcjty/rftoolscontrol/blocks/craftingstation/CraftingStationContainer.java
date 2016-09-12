package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import static mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity.*;

public class CraftingStationContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_OUTPUT = 0;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_OUTPUT, 56, 136, 9, 18, 1, 18);
            layoutPlayerInventorySlots(56, 157);
        }
    };



    public CraftingStationContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
