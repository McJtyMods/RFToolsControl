package mcjty.rftoolscontrol.modules.processor.logic;

import com.mojang.serialization.Codec;
import mcjty.rftoolscontrol.modules.processor.logic.grid.GridPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.HashMap;
import java.util.Map;

public enum Connection {
    UP("U"),
    DOWN("D"),
    LEFT("L"),
    RIGHT("R"),
    UP_NEG("u"),
    DOWN_NEG("d"),
    LEFT_NEG("l"),
    RIGHT_NEG("r");

    private final String id;

    public static final Codec<Connection> CODEC = Codec.STRING.xmap(Connection::getConnection, Connection::name);
    public static final StreamCodec<FriendlyByteBuf, Connection> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Connection.class);

    private static final Map<String, Connection> ID_TO_CONNECTION = new HashMap<>();

    static {
        for (Connection connection : values()) {
            ID_TO_CONNECTION.put(connection.getId(), connection);
        }
    }

    Connection(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Connection getConnection(String id) {
        return ID_TO_CONNECTION.get(id);
    }

    public boolean isPrimary() {
        return id.equals(id.toUpperCase());
    }

    public GridPos offset(GridPos coordinate) {
        return switch (this) {
            case UP_NEG, UP -> GridPos.pos(coordinate.x(), coordinate.y() - 1);
            case DOWN_NEG, DOWN -> GridPos.pos(coordinate.x(), coordinate.y() + 1);
            case LEFT_NEG, LEFT -> GridPos.pos(coordinate.x() - 1, coordinate.y());
            case RIGHT_NEG, RIGHT -> GridPos.pos(coordinate.x() + 1, coordinate.y());
        };
    }

    public Connection getOpposite() {
        return switch (this) {
            case UP -> UP_NEG;
            case DOWN -> DOWN_NEG;
            case LEFT -> LEFT_NEG;
            case RIGHT -> RIGHT_NEG;
            case UP_NEG -> UP;
            case DOWN_NEG -> DOWN;
            case LEFT_NEG -> LEFT;
            case RIGHT_NEG -> RIGHT;
        };
    }
}
