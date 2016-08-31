package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.logic.Connection;
import mcjty.rftoolscontrol.logic.GridInstance;
import mcjty.rftoolscontrol.logic.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.Opcode;
import mcjty.rftoolscontrol.logic.registry.OpcodeInput;
import mcjty.rftoolscontrol.logic.registry.Opcodes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompiledCard {

    private List<CompiledOpcode> opcodes = new ArrayList<>();

    public static CompiledCard compile(ProgramCardInstance cardInstance) {
        CompiledCard card = new CompiledCard();

        Map<Pair<Integer, Integer>, GridInstance> gridInstances = cardInstance.getGridInstances();

        for (Map.Entry<Pair<Integer, Integer>, GridInstance> entry : gridInstances.entrySet()) {
            Pair<Integer, Integer> location = entry.getKey();
            GridInstance grid = entry.getValue();
            String id = grid.getId();
            List<Connection> connections = grid.getConnections();
            Pair<Integer, Integer> primaryOutput = null;
            Pair<Integer, Integer> secondaryOutput = null;
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

    private static List<Pair<Integer, Integer>> findEvents(ProgramCardInstance cardInstance) {
        Map<Pair<Integer, Integer>, GridInstance> gridInstances = cardInstance.getGridInstances();
        List<Pair<Integer, Integer>> events = new ArrayList<>();
        for (Map.Entry<Pair<Integer, Integer>, GridInstance> entry : gridInstances.entrySet()) {
            String id = entry.getValue().getId();
            Opcode opcode = Opcodes.OPCODES.get(id);
            if (opcode.getOpcodeInput() == OpcodeInput.NONE) {
                events.add(entry.getKey());
            }
        }

        return events;
    }
}
