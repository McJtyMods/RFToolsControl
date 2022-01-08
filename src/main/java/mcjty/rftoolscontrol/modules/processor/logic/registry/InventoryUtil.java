package mcjty.rftoolscontrol.modules.processor.logic.registry;

import mcjty.rftoolsbase.api.control.parameters.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;

public class InventoryUtil {

    public static void writeBuf(Inventory inv, FriendlyByteBuf buf) {
        buf.writeUtf(inv.getNodeNameSafe());
        buf.writeByte(inv.getSide().ordinal());
        buf.writeByte(inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal());
    }

    public static Inventory readBuf(FriendlyByteBuf buf) {
        String nodeName = buf.readUtf(32767);
        int sideIdx = buf.readByte();
        Direction side = Direction.values()[sideIdx];
        sideIdx = buf.readByte();
        Direction intSide = sideIdx == -1 ? null : Direction.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public static Inventory readFromNBT(CompoundTag tag) {
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

    public static CompoundTag writeToNBT(Inventory inv) {
        CompoundTag tag = new CompoundTag();
        if (inv.hasNodeName()) {
            tag.putString("node", inv.getNodeName());
        }
        tag.putByte("side", (byte) inv.getSide().ordinal());
        tag.putByte("intside", (byte) (inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal()));
        return tag;
    }
}
