package mcjty.rftoolscontrol.logic.registry;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * How to identify a side on a block
 */
public class BlockSide {
    @Nullable private final String nodeName;          // An inventory on a network
    @Nullable private final EnumFacing side;      // The side at which the inventory can be found

    public BlockSide(@Nullable String name, @Nullable EnumFacing side) {
        this.nodeName = name;
        this.side = side;
    }

    @Nullable
    public String getNodeName() {
        return nodeName;
    }

    @Nullable
    public EnumFacing getSide() {
        return side;
    }

    @Override
    public String toString() {
        if (side == null) {
            return "*";
        } else {
            return side.toString();
        }
    }
}
