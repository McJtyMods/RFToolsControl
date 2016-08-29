package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
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
            Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
            ChoiceLabel label = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                    .addChoiceEvent((parent,newChoice) -> {
                        callback.valueChanged(ParameterValue.constant(parseFacingSafe(newChoice)));
                    })
                    .setDesiredWidth(60);
            label.setChoice(getFacingSafe(parameter, data));
            constantPanel.addChild(label);

            Panel variablePanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
            TextField variableIndex = new TextField(mc, gui);
            variablePanel.addChild(new Label(mc, gui).setText("Index:")).addChild(variableIndex);

            Panel functionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());

            TabbedPanel tabbedPanel = new TabbedPanel(mc, gui)
                    .addPage("Constant", constantPanel)
                    .addPage("Variable", variablePanel)
                    .addPage("Function", functionPanel);

            Panel buttonPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
            ToggleButton constantButton = new ToggleButton(mc, gui).setText("Constant")
                    .addButtonEvent(w -> switchPage(tabbedPanel, buttonPanel, "Constant"));
            ToggleButton variableButton = new ToggleButton(mc, gui).setText("Variable")
                    .addButtonEvent(w -> switchPage(tabbedPanel, buttonPanel, "Variable"));
            ToggleButton functionButton = new ToggleButton(mc, gui).setText("Function")
                    .addButtonEvent(w -> switchPage(tabbedPanel, buttonPanel, "Function"));
            buttonPanel.addChild(constantButton).addChild(variableButton).addChild(functionButton);

            panel.addChild(buttonPanel).addChild(tabbedPanel);
            ParameterValue value = (ParameterValue) data.get(parameter.getName());

            if (value != null) {
                if (value.isConstant()) {
                    switchPage(tabbedPanel, buttonPanel, "Constant");
                } else {
                    switchPage(tabbedPanel, buttonPanel, "Variable");
                }
            }
        });
    }

    private static void switchPage(TabbedPanel tabbedPanel, Panel buttonPanel, String page) {
        for (int i = 0 ; i < buttonPanel.getChildCount() ; i++) {
            ToggleButton button = (ToggleButton) buttonPanel.getChild(i);
            if (!page.equals(button.getText())) {
                button.setPressed(false);
            } else {
                button.setPressed(true);
            }
            tabbedPanel.setCurrent(page);
        }

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
