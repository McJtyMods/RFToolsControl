package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ParameterEditors {

    private static final Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

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
        EDITORS.put(ParameterType.PAR_SIDE, (mc, gui, panel, callback, parameter, data) -> {
            ChoiceLabel label = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                    .addChoiceEvent((parent,newChoice) -> {
                        callback.valueChanged(ParameterValue.constant(parseFacingSafe(newChoice)));
                    })
                    .setDesiredWidth(60);
            label.setChoice(getFacingSafe(parameter, data));
            panel.addChild(label);
        });
    }

    private static EnumFacing parseFacingSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return EnumFacing.byName(StringUtils.lowerCase(t));
    }

    private static String getFacingSafe(ParameterDescription parameter, Map<String, Object> data) {
        ParameterValue value = (ParameterValue) data.get(parameter.getName());
        String choice = "*";
        if (value != null) {
            if (value.isConstant()) {
                if (value.getValue() != null) {
                    choice = StringUtils.capitalize(value.getValue().toString());
                }
            } else {
                // @todo variable support
            }
        }
        return choice;
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
        ParameterValue value = (ParameterValue) data.get(parameter.getName());
        if (value == null) {
            return "";
        }
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
