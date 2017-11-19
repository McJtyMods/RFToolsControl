package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.network.PacketUpdateNBTItemInventory;
import mcjty.lib.network.PacketUpdateNBTItemInventoryHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketUpdateNBTItemInventoryProgrammer extends PacketUpdateNBTItemInventory {

    public PacketUpdateNBTItemInventoryProgrammer() {
    }

    public PacketUpdateNBTItemInventoryProgrammer(BlockPos pos, int slotIndex, NBTTagCompound tagCompound) {
        super(pos, slotIndex, tagCompound);
    }

    @Override
    protected boolean isValidBlock(World world, BlockPos blockPos, TileEntity tileEntity) {
        return tileEntity instanceof ProgrammerTileEntity;
    }

    public static class Handler extends PacketUpdateNBTItemInventoryHandler<PacketUpdateNBTItemInventoryProgrammer> {

    }
}
