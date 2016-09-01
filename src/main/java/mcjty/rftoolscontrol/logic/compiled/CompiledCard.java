package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.grid.GridInstance;
import mcjty.rftoolscontrol.logic.grid.GridPos;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.Opcode;
import mcjty.rftoolscontrol.logic.registry.OpcodeInput;
import mcjty.rftoolscontrol.logic.registry.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledCard {

    private List<CompiledOpcode> opcodes = new ArrayList<>();

    public static CompiledCard compile(ProgramCardInstance cardInstance) {
        CompiledCard card = new CompiledCard();

        Map<GridPos, GridInstance> gridInstances = cardInstance.getGridInstances();

        // First find the indices of all compiled grid instances
        Map<GridPos, Integer> posToIndex = new HashMap<>();
        int idx = 0;
        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos location = entry.getKey();
            posToIndex.put(location, idx);
            idx++;
        }
        // Index of a dummy stop opcode that we can use to go too in case there is no real connection
        int stopIdx = idx;

        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos location = entry.getKey();
            GridInstance grid = entry.getValue();
            String id = grid.getId();
            Opcode opcode = Opcodes.OPCODES.get(id);

            GridPos primaryOutput = grid.getPrimaryConnection() != null ? grid.getPrimaryConnection().offset(location) : null;
            GridPos secondaryOutput = grid.getSecondaryConnection() != null ? grid.getSecondaryConnection().offset(location) : null;
            CompiledOpcode.Builder opcodeBuilder = CompiledOpcode.builder().opcode(opcode);
            if (primaryOutput != null && posToIndex.containsKey(primaryOutput)) {
                opcodeBuilder.primaryIndex(posToIndex.get(primaryOutput));
            } else {
                opcodeBuilder.primaryIndex(stopIdx);
            }
            if (secondaryOutput != null && posToIndex.containsKey(secondaryOutput)) {
                opcodeBuilder.secondaryIndex(posToIndex.get(secondaryOutput));
            } else {
                opcodeBuilder.secondaryIndex(stopIdx);
            }
            for (Parameter parameter : grid.getParameters()) {
                opcodeBuilder.parameter(parameter);
            }
            card.opcodes.add(opcodeBuilder.build());
        }
        card.opcodes.add(CompiledOpcode.builder().opcode(Opcodes.DO_STOP).build());

        return card;
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
