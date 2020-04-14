package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class FloatEditor extends AbstractParameterEditor {

    private TextField field;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        field = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());
        constantPanel.children(field);

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
            field.text("");
        } else {
            try {
                field.text(Float.toString((Float) value.getValue()));
            } catch (Exception e) {
                field.text("");
            }
        }
    }

}
