package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class BooleanEditor extends AbstractParameterEditor {

    private ChoiceLabel label;

    private static Boolean parseBoolSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return "true".equals(t);
    }

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        label = new ChoiceLabel().choices("*", "true", "false")
                .event((newChoice) -> callback.valueChanged(readValue()))
                .desiredWidth(60);
        constantPanel.children(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_BOOLEAN);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseBoolSafe(label.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            label.choice("*");
        } else {
            String choice = value.getValue().toString();
            label.choice(choice);
        }
    }
}
