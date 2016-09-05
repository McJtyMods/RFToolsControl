package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.util.Map;

public class NodeTileEntity extends GenericTileEntity implements ITickable {

    public static final String CMD_UPDATE = "update";

    private String channel;
    private String node;

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    public String getNodeName() {
        return node;
    }

    public String getChannelName() {
        return channel;
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            prevIn = powerLevel;
        }
    }

    public int getPowerOut(EnumFacing side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(EnumFacing side, int powerOut) {
        this.powerOut[side.ordinal()] = powerOut;
        markDirty();
        worldObj.notifyBlockOfStateChange(this.pos.offset(side), this.getBlockType());
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
