package mcjty.rftoolscontrol.logic;

import java.util.ArrayList;
import java.util.List;

public class GridInstance {

    private final String id;
    private List<Connection> connections = new ArrayList<>();

    public GridInstance(String id) {
        this.id = id;
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    public String getId() {
        return id;
    }

    public List<Connection> getConnections() {
        return connections;
    }
}
