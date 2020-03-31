package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class FloatEditor extends AbstractParameterEditor {

    private TextField field;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        field = new TextField(mc, gui)
                .addTextEvent((parent, newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((parent, newText) -> closeWindow());
        constantPanel.addChild(field);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_FLOAT);
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(field);
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
