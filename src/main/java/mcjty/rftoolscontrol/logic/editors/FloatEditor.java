package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.api.paremeters.ParameterType;
import mcjty.rftoolscontrol.api.paremeters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class FloatEditor extends AbstractParameterEditor {

    private static Float parseFloatSafe(String newText) {
        Float f;
        try {
            f = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            f = null;
        }
        return f;
    }

    private TextField field;

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        field = new TextField(mc, gui).addTextEvent((parent, newText) -> callback.valueChanged(readValue()));
        constantPanel.addChild(field);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_FLOAT);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseFloatSafe(field.getText()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            field.setText("");
        } else {
            try {
                field.setText(Float.toString((Float) value.getValue()));
            } catch (Exception e) {
                field.setText("");
            }
        }
    }

}
