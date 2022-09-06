package mcjty.rftoolscontrol.modules.processor.logic.compiled;

import mcjty.rftoolsbase.api.control.code.Opcode;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterDescription;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import mcjty.rftoolscontrol.modules.processor.logic.grid.GridInstance;
import mcjty.rftoolscontrol.modules.processor.logic.grid.GridPos;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;

import javax.annotation.Nonnull;
import java.util.*;

public class CompiledCard {

    private final List<CompiledOpcode> opcodes = new ArrayList<>();
    private final Map<Opcode, List<CompiledEvent>> events = new HashMap<>();

    public static CompiledCard compile(ProgramCardInstance cardInstance) {
        if (cardInstance == null) {
            return null;
        }

        CompiledCard card = new CompiledCard();

        Map<GridPos, GridInstance> gridInstances = cardInstance.getGridInstances();

        // First find the indices of all compiled grid instances
        Map<GridPos, Integer> posToIndex = new HashMap<>();
        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos location = entry.getKey();
            posToIndex.put(location, posToIndex.size());
        }
        // Index of a dummy stop opcode that we can use to go too in case there is no real connection
        int stopIdx = posToIndex.size();

        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos location = entry.getKey();
            GridInstance grid = entry.getValue();
            String id = grid.getId();
            Opcode opcode = Opcodes.OPCODES.get(id);

            GridPos primaryOutput = grid.getPrimaryConnection() != null ? grid.getPrimaryConnection().offset(location) : null;
            GridPos secondaryOutput = grid.getSecondaryConnection() != null ? grid.getSecondaryConnection().offset(location) : null;
            CompiledOpcode.Builder opcodeBuilder = CompiledOpcode.builder().opcode(opcode);
            opcodeBuilder.grid(location.x(), location.y());
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
            List<ParameterDescription> parameters = opcode.getParameters();
            boolean single = false;
            for (int i = 0 ; i < grid.getParameters().size() ; i++) {
                Parameter parameter = grid.getParameters().get(i);
                if (i < parameters.size() && "single".equals(parameters.get(i).getName())) {
                    single = TypeConverters.convertToBool(parameter);
                }
                opcodeBuilder.parameter(parameter);
            }

            if (opcode.isEvent()) {
                card.events.putIfAbsent(opcode, new ArrayList<>());
                card.events.get(opcode).add(new CompiledEvent(card.opcodes.size(), single));
            }

            card.opcodes.add(opcodeBuilder.build());
        }
        card.opcodes.add(CompiledOpcode.builder().opcode(Opcodes.DO_STOP_OR_RESUME).build());

        for (Opcode opcode : Opcodes.OPCODES.values()) {
            if (!card.events.containsKey(opcode)) {
                card.events.put(opcode, Collections.emptyList());
            }
        }


        return card;
    }

    @Nonnull
    public List<CompiledOpcode> getOpcodes() {
        return opcodes;
    }

    @Nonnull
    public List<CompiledEvent> getEvents(Opcode opcode) {
        return events.get(opcode);
    }
}
