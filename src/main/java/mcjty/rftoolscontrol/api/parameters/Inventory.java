package mcjty.rftoolscontrol.api.parameters;

import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class identifies an inventory on a network. It has an optional
 * node name. If that is not given then the processor itself is meant.
 * There is also a side adjacent to the node or processor and an
 * optional internal side. The internal side represents from which side
 * we are supposedly accessing the inventory.
 */
public class Inventory extends BlockSide {
    @Nullable private final EnumFacing intSide;   // The side at which we are accessing the inventory (can be null)

    public Inventory(@Nullable String name, @Nonnull EnumFacing side, @Nullable EnumFacing intSide) {
        super(name, side);
        this.intSide = intSide;
    }

    public String serialize() {
        return "#" + (hasNodeName() ? getNodeName() : "-") + "#" + getSide().getName() + "#" + (intSide == null ? "-" : intSide.getName()) + "#";
    }

    public static Inventory deserialize(String s) {
        String[] splitted = StringUtils.split(s, '#');
        return new Inventory("-".equals(splitted[0]) ? null : splitted[0], EnumFacing.byName(splitted[1]),
                "-".equals(splitted[2]) ? null : EnumFacing.byName(splitted[2]));
    }

    @Nonnull
    public EnumFacing getSide() {
        return super.getSide();
    }

    @Nullable
    public EnumFacing getIntSide() {
        return intSide;
    }

    public String getStringRepresentation() {
        String s = StringUtils.left(getSide().getName().toUpperCase(), 1);
        if (getIntSide() == null) {
            s += "/*";
        } else {
            String is = StringUtils.left(getIntSide().getName().toUpperCase(), 1);
            s += "/" + is;
        }
        if (getNodeName() == null) {
            return s;
        } else {
            return StringUtils.left(getNodeName(), 6) + " " + s;
        }
    }

    @Override
    public String toString() {
        String s = StringUtils.left(getSide().getName().toUpperCase(), 1);
        if (getIntSide() == null) {
            s += "/*";
        } else {
            String is = StringUtils.left(getIntSide().getName().toUpperCase(), 1);
            s += "/" + is;
        }
        if (getNodeName() == null) {
            return s;
        } else {
            return getNodeName() + " " + s;
        }
    }

    @Nullable
    public static Inventory fromString(String s) {
        if (s == null) {
            return null;
        }
        int indexOf = s.lastIndexOf('/');
        if (indexOf == -1) {
            return null;
        }
        if (s.length() <= indexOf+1) {
            return null;
        }
        EnumFacing side = getSideFromChar(s.charAt(indexOf-1));
        if (side == null) {
            return null;
        }
        EnumFacing intSide = getSideFromChar(s.charAt(indexOf+1));
        int i = indexOf-2;
        String name;
        if (i <= 0) {
            name = null;
        } else {
            name = s.substring(0, i);
        }

        return new Inventory(name, side, intSide);
    }

    public static EnumFacing getSideFromChar(char is) {
        switch (is) {
            case '*': return null;
            case 'D': return EnumFacing.DOWN;
            case 'U': return EnumFacing.UP;
            case 'W': return EnumFacing.WEST;
            case 'E': return EnumFacing.EAST;
            case 'S': return EnumFacing.SOUTH;
            case 'N': return EnumFacing.NORTH;
        }
        return null;
    }
}
