package mcjty.rftoolscontrol.logic;

import org.apache.commons.lang3.tuple.Pair;

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

    private static Map<String, Connection> ID_TO_CONNECTION = new HashMap<>();

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

    public Pair<Integer,Integer> offset(Pair<Integer,Integer> coordinate) {
        switch (this) {
            case UP_NEG:
            case UP:
                return Pair.of(coordinate.getLeft(), coordinate.getRight()-1);
            case DOWN_NEG:
            case DOWN:
                return Pair.of(coordinate.getLeft(), coordinate.getRight()+1);
            case LEFT_NEG:
            case LEFT:
                return Pair.of(coordinate.getLeft()-1, coordinate.getRight());
            case RIGHT_NEG:
            case RIGHT:
                return Pair.of(coordinate.getLeft()+1, coordinate.getRight());
            default:
                return coordinate;
        }
    }

    public Connection getOpposite() {
        switch (this) {
            case UP: return UP_NEG;
            case DOWN: return DOWN_NEG;
            case LEFT: return LEFT_NEG;
            case RIGHT: return RIGHT_NEG;
            case UP_NEG: return UP;
            case DOWN_NEG: return DOWN;
            case LEFT_NEG: return LEFT;
            case RIGHT_NEG: return RIGHT;
        }
        return this;
    }
}
