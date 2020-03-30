package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolsbase.api.control.parameters.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class InventoryUtil {

    public static void writeBuf(Inventory inv, PacketBuffer buf) {
        buf.writeString(inv.getNodeNameSafe());
        buf.writeByte(inv.getSide().ordinal());
        buf.writeByte(inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal());
    }

    public static Inventory readBuf(PacketBuffer buf) {
        String nodeName = buf.readString(32767);
        int sideIdx = buf.readByte();
        Direction side = Direction.values()[sideIdx];
        sideIdx = buf.readByte();
        Direction intSide = sideIdx == -1 ? null : Direction.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static Inventory readFromNBT(CompoundNBT tag) {
        String nodeName = null;
        if (tag.contains("node")) {
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
            tag.putString("node", inv.getNodeName());
        }
        tag.putByte("side", (byte) inv.getSide().ordinal());
        tag.putByte("intside", (byte) (inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal()));
        return tag;
    }
}
