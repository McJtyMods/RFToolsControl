package mcjty.rftoolscontrol.logic.registry;

import net.minecraft.util.EnumFacing;

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
        this.nodeName = name;
        this.side = side;
        this.intSide = intSide;
    }

    @Nullable
    public String getNodeName() {
        return nodeName;
    }

    @Nonnull
    public EnumFacing getSide() {
        return side;
    }

    @Nullable
    public EnumFacing getIntSide() {
        return intSide;
    }
}
