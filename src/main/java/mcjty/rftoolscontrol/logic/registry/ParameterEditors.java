package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.logic.Parameter;

import java.util.HashMap;
import java.util.Map;

public class ParameterEditors {

    private final static Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

    public static void init() {
        EDITORS.put(ParameterType.PAR_FLOAT, (mc, gui, panel, callback, parameter, data) -> {
            TextField field = new TextField(mc, gui).addTextEvent((parent, newText) -> {
                callback.valueChanged(ParameterValue.constant(parseFloatSafe(newText)));
            });
            field.setText(getValueSafe(parameter, data));
            panel.addChild(field);
        });
        EDITORS.put(ParameterType.PAR_INTEGER, (mc, gui, panel, callback, parameter, data) -> {
            TextField field = new TextField(mc, gui).addTextEvent((parent, newText) -> {
                callback.valueChanged(ParameterValue.constant(parseIntSafe(newText)));
            });
            field.setText(getValueSafe(parameter, data));
            panel.addChild(field);
        });
    }

    private static Float parseFloatSafe(String newText) {
        Float f;
        try {
            f = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            f = 0.0f;
        }
        return f;
    }

    private static Integer parseIntSafe(String newText) {
        Integer f;
        try {
            f = Integer.parseInt(newText);
        } catch (NumberFormatException e) {
            f = 0;
        }
        return f;
    }

    private static String getValueSafe(ParameterDescription parameter, Map<String, Object> data) {
        Parameter par = (Parameter) data.get(parameter.getName());
        if (par == null) {
            return "ERR";
        }
        ParameterValue value = par.getParameterValue();
        if (value.isConstant()) {
            if (value.getValue() == null) {
                return "";
            } else {
                return value.getValue().toString();
            }
        } else {
            // @todo variable support
            return "VAR";
        }
    }

    public static ParameterEditor getEditor(ParameterType type) {
        return EDITORS.get(type);
    }
}
