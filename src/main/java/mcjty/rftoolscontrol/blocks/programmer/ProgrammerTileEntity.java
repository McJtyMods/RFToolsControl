package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class ProgrammerTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProgrammerContainer.factory, 2);

    public ProgrammerTileEntity() {
        inventoryHelper.setStackInSlot(ProgrammerContainer.SLOT_DUMMY, new ItemStack(ModItems.programCardItem));
    }

    @Override
    public void setPowerInput(int powered) {
        int p = powerLevel;
        super.setPowerInput(powered);
        if (p != powerLevel && powerLevel > 0) {
            // Copy program in programmer to card
            ItemStack dummy = getStackInSlot(ProgrammerContainer.SLOT_DUMMY);
            if (dummy.isEmpty()) {
                return;
            }
            if (!dummy.hasTagCompound()) {
                return;
            }
            ItemStack card = getStackInSlot(ProgrammerContainer.SLOT_CARD);
            if (card.isEmpty()) {
                return;
            }
            ProgramCardInstance instance = ProgramCardInstance.parseInstance(dummy);
            if (instance == null) {
                return;
            }
            ProgramCardItem.setCardName(card, ProgramCardItem.getCardName(dummy));
            NBTBase newGrid = dummy.getTagCompound().getTag("grid").copy();
            card.getTagCompound().setTag("grid", newGrid);
        }
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }
}
