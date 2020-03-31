package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

import static mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup.CRAFTING_STATION_CONTAINER;

public class CraftingStationContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_OUTPUT = 0;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(9) {
        @Override
        protected void setup() {
            box(SlotDefinition.container(), CONTAINER_INVENTORY, SLOT_OUTPUT, 6, 157, 3, 3);
            playerSlots(66, 157);
        }
    };

    public CraftingStationContainer(int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te) {
        super(CRAFTING_STATION_CONTAINER.get(), id, factory, pos, te);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(CONTAINER_INVENTORY, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots();
    }
}
