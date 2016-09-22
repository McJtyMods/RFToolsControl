package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.api.paremeters.ParameterType;
import mcjty.rftoolscontrol.api.paremeters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class StringEditor extends AbstractParameterEditor {

    private TextField field;

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        field = new TextField(mc, gui).addTextEvent((parent, newText) -> callback.valueChanged(readValue()));
        constantPanel.addChild(field);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_STRING);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(field.getText());
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            field.setText("");
        } else {
            try {
                field.setText(value.getValue().toString());
            } catch (Exception e) {
                field.setText("");
            }
        }
    }
}
