package mcjty.rftoolscontrol.logic.editors;

import mcjty.rftoolscontrol.logic.registry.ParameterType;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class ParameterEditors {

    private static final Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

    public static void init() {
        EDITORS.put(PAR_FLOAT, new FloatEditor());
        EDITORS.put(PAR_INTEGER, new IntegerEditor());
        EDITORS.put(PAR_STRING, new StringEditor());
        EDITORS.put(PAR_SIDE, new SideEditor());
        EDITORS.put(PAR_BOOLEAN, new BooleanEditor());
        EDITORS.put(PAR_INVENTORY, new InventoryEditor());
        EDITORS.put(PAR_ITEM, new ItemEditor());
    }

    public static ParameterEditor getEditor(ParameterType type) {
        return EDITORS.get(type);
    }

}
