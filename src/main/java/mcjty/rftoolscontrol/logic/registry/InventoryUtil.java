package mcjty.rftoolscontrol.logic.registry;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class InventoryUtil {

    public static void writeBuf(Inventory inv, ByteBuf buf) {
        NetworkTools.writeString(buf, inv.getNodeName());
        buf.writeByte(inv.getSide().ordinal());
        buf.writeByte(inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal());
    }

    public static Inventory readBuf(ByteBuf buf) {
        String nodeName = NetworkTools.readString(buf);
        int sideIdx = buf.readByte();
        Direction side = Direction.values()[sideIdx];
        sideIdx = buf.readByte();
        Direction intSide = sideIdx == -1 ? null : Direction.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static Inventory readFromNBT(CompoundNBT tag) {
        String nodeName = null;
        if (tag.hasKey("node")) {
            nodeName = tag.getString("node");
        }
        int sideIdx = tag.getByte("side");
        Direction side = Direction.values()[sideIdx];
        sideIdx = tag.getByte("intside");
        Direction intSide = sideIdx == -1 ? null : Direction.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static CompoundNBT writeToNBT(Inventory inv) {
        CompoundNBT tag = new CompoundNBT();
        if (inv.hasNodeName()) {
            tag.setString("node", inv.getNodeName());
        }
        tag.setByte("side", (byte) inv.getSide().ordinal());
        tag.setByte("intside", (byte) (inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal()));
        return tag;
    }
}
