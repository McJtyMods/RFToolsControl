package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class NodeTileEntity extends GenericTileEntity {

    public static final String CMD_UPDATE = "update";

    private String channel;
    private String node;

    public String getNodeName() {
        return node;
    }

    public String getChannelName() {
        return channel;
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
