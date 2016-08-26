package mcjty.rftoolscontrol.blocks;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.entity.player.EntityPlayer;

public class ProgrammerTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProgrammerContainer.factory, 1);

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }
}
