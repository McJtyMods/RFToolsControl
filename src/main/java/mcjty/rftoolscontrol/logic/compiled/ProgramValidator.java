package mcjty.rftoolscontrol.logic.compiled;

import mcjty.rftoolscontrol.api.code.Opcode;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterDescription;
import mcjty.rftoolscontrol.logic.grid.GridInstance;
import mcjty.rftoolscontrol.logic.grid.GridPos;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.Opcodes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ProgramValidator {

    public static List<Pair<GridPos, String>> validate(ProgramCardInstance program) {
        List<Pair<GridPos, String>> errors = new ArrayList<>();

        Map<GridPos, GridInstance> grid = program.getGridInstances();

        // Find all unreachable instances:
        Set<GridPos> reachableLocations = new HashSet<>();
        for (Map.Entry<GridPos, GridInstance> entry : grid.entrySet()) {
            GridInstance g = entry.getValue();
            if (g.getPrimaryConnection() != null) {
                reachableLocations.add(g.getPrimaryConnection().offset(entry.getKey()));
            }
            if (g.getSecondaryConnection() != null) {
                reachableLocations.add(g.getSecondaryConnection().offset(entry.getKey()));
            }
        }
        for (Map.Entry<GridPos, GridInstance> entry : grid.entrySet()) {
            GridInstance g = entry.getValue();
            Opcode opcode = Opcodes.OPCODES.get(g.getId());
            GridPos p = entry.getKey();
            if (!opcode.isEvent() && !reachableLocations.contains(p)) {
                errors.add(Pair.of(p, "Unreachable: " + p.getX() + "," + p.getY()));
            }
        }

        // Find all missing required parameters:
        for (Map.Entry<GridPos, GridInstance> entry : grid.entrySet()) {
            GridPos p = entry.getKey();
            GridInstance g = entry.getValue();
            Opcode opcode = Opcodes.OPCODES.get(g.getId());
            List<ParameterDescription> descriptions = opcode.getParameters();
            List<Parameter> parameters = g.getParameters();
            for (int i = 0 ; i < descriptions.size() ; i++) {
                ParameterDescription desc = descriptions.get(i);
                Parameter par = i < parameters.size() ? parameters.get(i) : null;
                if (!desc.isOptional()) {
                    if (par == null || par.getParameterValue() == null || (par.getParameterValue().isConstant() && par.getParameterValue().getValue() == null)) {
                        errors.add(Pair.of(p, "Missing parameter (" + desc.getName() + "): " + p.getX() + "," + p.getY()));
                    }
                }
            }
        }

        return errors;
    }
}
