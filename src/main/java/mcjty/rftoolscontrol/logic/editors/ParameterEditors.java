package mcjty.rftoolscontrol.logic.editors;

import mcjty.rftoolsbase.api.control.parameters.ParameterType;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftoolsbase.api.control.parameters.ParameterType.*;

public class ParameterEditors {

    private static final Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

    public static void init() {
        EDITORS.put(PAR_FLOAT, new FloatEditor());
        EDITORS.put(PAR_INTEGER, new IntegerEditor());
        EDITORS.put(PAR_LONG, new LongEditor());
        EDITORS.put(PAR_STRING, new StringEditor());
        EDITORS.put(PAR_SIDE, new SideEditor());
        EDITORS.put(PAR_BOOLEAN, new BooleanEditor());
        EDITORS.put(PAR_INVENTORY, new InventoryEditor());
        EDITORS.put(PAR_ITEM, new ItemEditor());
        EDITORS.put(PAR_FLUID, new FluidEditor());
        EDITORS.put(PAR_EXCEPTION, new ExceptionEditor());
        EDITORS.put(PAR_TUPLE, new TupleEditor());
        EDITORS.put(PAR_VECTOR, new VectorEditor());
        EDITORS.put(PAR_NUMBER, new NumberEditor());
    }

    public static ParameterEditor getEditor(ParameterType type) {
        return EDITORS.get(type);
    }

}
