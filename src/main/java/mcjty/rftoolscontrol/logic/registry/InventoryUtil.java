package mcjty.rftoolscontrol.logic.registry;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class InventoryUtil {

    public static void writeBuf(Inventory inv, ByteBuf buf) {
        NetworkTools.writeString(buf, inv.getNodeName());
        buf.writeByte(inv.getSide().ordinal());
        buf.writeByte(inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal());
    }

    public static Inventory readBuf(ByteBuf buf) {
        String nodeName = NetworkTools.readString(buf);
        int sideIdx = buf.readByte();
        EnumFacing side = EnumFacing.values()[sideIdx];
        sideIdx = buf.readByte();
        EnumFacing intSide = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static Inventory readFromNBT(NBTTagCompound tag) {
        String nodeName = null;
        if (tag.hasKey("node")) {
            nodeName = tag.getString("node");
        }
        int sideIdx = tag.getByte("side");
        EnumFacing side = EnumFacing.values()[sideIdx];
        sideIdx = tag.getByte("intside");
        EnumFacing intSide = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static NBTTagCompound writeToNBT(Inventory inv) {
        NBTTagCompound tag = new NBTTagCompound();
        if (inv.hasNodeName()) {
            tag.setString("node", inv.getNodeName());
        }
        tag.setByte("side", (byte) inv.getSide().ordinal());
        tag.setByte("intside", (byte) (inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal()));
        return tag;
    }
}
