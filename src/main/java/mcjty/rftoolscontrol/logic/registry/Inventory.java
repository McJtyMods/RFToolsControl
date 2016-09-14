package mcjty.rftoolscontrol.logic.registry;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * How to identify an inventory
 */
public class Inventory {
    @Nullable private final String nodeName;          // An inventory on a network
    @Nonnull private final EnumFacing side;      // The side at which the inventory can be found
    @Nullable private final EnumFacing intSide;   // The side at which we are accessing the inventory (can be null)

    public Inventory(@Nullable String name, @Nonnull EnumFacing side, @Nullable EnumFacing intSide) {
        this.nodeName = (name == null || name.isEmpty()) ? null : name;
        this.side = side;
        this.intSide = intSide;
    }

    @Nullable
    public String getNodeName() {
        return nodeName;
    }

    public boolean hasNodeName() {
        return nodeName != null && !nodeName.isEmpty();
    }

    public String serialize() {
        return "#" + (hasNodeName() ? nodeName : "-") + "#" + side.getName() + "#" + (intSide == null ? "-" : intSide.getName()) + "#";
    }

    public static Inventory deserialize(String s) {
        String[] splitted = StringUtils.split(s, '#');
        return new Inventory("-".equals(splitted[1]) ? null : splitted[1], EnumFacing.byName(splitted[1]),
                "-".equals(splitted[2]) ? null : EnumFacing.byName(splitted[2]));
    }

    @Nonnull
    public EnumFacing getSide() {
        return side;
    }

    @Nullable
    public EnumFacing getIntSide() {
        return intSide;
    }

    public void writeBuf(ByteBuf buf) {
        NetworkTools.writeString(buf, getNodeName());
        buf.writeByte(getSide().ordinal());
        buf.writeByte(getIntSide() == null ? -1 : getIntSide().ordinal());
    }

    public static Inventory readBuf(ByteBuf buf) {
        String nodeName = NetworkTools.readString(buf);
        int sideIdx = buf.readByte();
        EnumFacing side = EnumFacing.values()[sideIdx];
        sideIdx = buf.readByte();
        EnumFacing intSide = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
        return new Inventory(nodeName, side, intSide);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (hasNodeName()) {
            tag.setString("node", getNodeName());
        }
        tag.setByte("side", (byte) getSide().ordinal());
        tag.setByte("intside", (byte) (getIntSide() == null ? -1 : getIntSide().ordinal()));
        return tag;
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
}
