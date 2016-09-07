package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CraftingStationTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CraftingStationContainer.factory, 1);

    private List<BlockPos> processorList = new ArrayList<>();

    @Override
    public void update() {
    }

    public void registerProcessor(BlockPos pos) {
        if (!processorList.contains(pos)) {
            processorList.add(pos);
        }
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        NBTTagList list = tagCompound.getTagList("processors", Constants.NBT.TAG_COMPOUND);
        processorList.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            processorList.add(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        NBTTagList list = new NBTTagList();
        for (BlockPos p : processorList) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", p.getX());
            tag.setInteger("y", p.getY());
            tag.setInteger("z", p.getZ());
            list.appendTag(tag);
        }
        tagCompound.setTag("processors", list);
        return tagCompound;
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

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }
}
