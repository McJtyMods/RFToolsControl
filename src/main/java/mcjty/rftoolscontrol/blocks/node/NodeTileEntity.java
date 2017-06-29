package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class NodeTileEntity extends GenericTileEntity {

    public static final String CMD_UPDATE = "update";

    private String channel;
    private String node;

    private BlockPos processor = null;

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    public String getNodeName() {
        return node;
    }

    public String getChannelName() {
        return channel;
    }

    public BlockPos getProcessor() {
        return processor;
    }

    public void setProcessor(BlockPos processor) {
        this.processor = processor;
        markDirty();
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel != powered) {
            if (processor != null) {
                TileEntity te = getWorld().getTileEntity(processor);
                if (te instanceof ProcessorTileEntity) {
                    ProcessorTileEntity processorTileEntity = (ProcessorTileEntity) te;
                    processorTileEntity.redstoneNodeChange(prevIn, powered, node);
                }
            }
            prevIn = powered;
        }
        super.setPowerInput(powered);
    }

    public int getPowerOut(EnumFacing side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(EnumFacing side, int powerOut) {
        this.powerOut[side.ordinal()] = powerOut;
        markDirty();
        getWorld().neighborChanged(this.pos.offset(side), this.getBlockType(), this.pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getInteger("prevIn");
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.setByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getString("channel");
        node = tagCompound.getString("node");
        processor = BlockPosTools.readFromNBT(tagCompound, "processor");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (channel != null) {
            tagCompound.setString("channel", channel);
        }
        if (node != null) {
            tagCompound.setString("node", node);
        }
        if (processor != null) {
            BlockPosTools.writeToNBT(tagCompound, "processor", processor);
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_UPDATE.equals(command)) {
            this.node = args.get("node").getString();
            this.channel = args.get("channel").getString();
            markDirtyClient();
            return true;
        }
        return false;
    }

}
