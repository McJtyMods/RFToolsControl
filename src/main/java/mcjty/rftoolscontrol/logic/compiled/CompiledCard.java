package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.logic.Connection;
import mcjty.rftoolscontrol.logic.grid.GridInstance;
import mcjty.rftoolscontrol.logic.grid.GridPos;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.Opcode;
import mcjty.rftoolscontrol.logic.registry.OpcodeInput;
import mcjty.rftoolscontrol.logic.registry.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompiledCard {

    private List<CompiledOpcode> opcodes = new ArrayList<>();

    public static CompiledCard compile(ProgramCardInstance cardInstance) {
        CompiledCard card = new CompiledCard();

        Map<GridPos, GridInstance> gridInstances = cardInstance.getGridInstances();

        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos location = entry.getKey();
            GridInstance grid = entry.getValue();
            String id = grid.getId();
            List<Connection> connections = grid.getConnections();
            GridPos primaryOutput = null;
            GridPos secondaryOutput = null;
            Opcode opcode = Opcodes.OPCODES.get(id);
            switch (opcode.getOpcodeOutput()) {
                case NONE:
                    break;
                case SINGLE:
                    primaryOutput = findPrimaryConnection(connections).offset(location);    // @todo handle the case no connections are given
                    break;
                case YESNO:
                    primaryOutput = findPrimaryConnection(connections).offset(location);    // @todo handle the case no connections are given
                    secondaryOutput = findSecondaryConnection(connections).offset(location);    // @todo handle the case no connections are given
                    break;
            }
            //@todo
        }


        return card;
    }

    private static Connection findPrimaryConnection(List<Connection> connections) {
        for (Connection connection : connections) {
            if (!connection.getId().toLowerCase().equals(connection.getId())) {
                return connection;
            }
        }
        return null;
    }

    private static Connection findSecondaryConnection(List<Connection> connections) {
        for (Connection connection : connections) {
            if (connection.getId().toLowerCase().equals(connection.getId())) {
                return connection;
            }
        }
        return null;
    }

    private static List<GridPos> findEvents(ProgramCardInstance cardInstance) {
        Map<GridPos, GridInstance> gridInstances = cardInstance.getGridInstances();
        List<GridPos> events = new ArrayList<>();
        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            String id = entry.getValue().getId();
            Opcode opcode = Opcodes.OPCODES.get(id);
            if (opcode.getOpcodeInput() == OpcodeInput.NONE) {
                events.add(entry.getKey());
            }
        }

        return events;
    }
}
