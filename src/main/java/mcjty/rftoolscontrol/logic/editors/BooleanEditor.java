package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

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
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        label = new ChoiceLabel(mc, gui).addChoices("*", "true", "false")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_BOOLEAN);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseBoolSafe(label.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            label.setChoice("*");
        } else {
            String choice = value.getValue().toString();
            label.setChoice(choice);
        }
    }
}
