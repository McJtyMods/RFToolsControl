package mcjty.rftoolscontrol.logic.editors;

import mcjty.rftoolscontrol.logic.registry.ParameterType;

import java.util.HashMap;
import java.util.Map;

public class ParameterEditors {

    private static final Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

    public static void init() {
        EDITORS.put(ParameterType.PAR_FLOAT, new FloatEditor());
        EDITORS.put(ParameterType.PAR_INTEGER, new IntegerEditor());
        EDITORS.put(ParameterType.PAR_SIDE, new SideEditor());
    }

    public static ParameterEditor getEditor(ParameterType type) {
        return EDITORS.get(type);
    }

}
