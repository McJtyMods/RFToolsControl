package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolsbase.api.control.parameters.Tuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class TupleEditor extends AbstractParameterEditor {

    private TextField fieldX;
    private TextField fieldY;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        fieldX = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());
        fieldY = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());
        constantPanel.children(fieldX, fieldY);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_TUPLE);
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(fieldX);
    }

    @Override
    protected ParameterValue readConstantValue() {
        Integer x = parseIntSafe(fieldX.getText());
        Integer y = parseIntSafe(fieldY.getText());
        return ParameterValue.constant(new Tuple(x == null ? 0 : x, y == null ? 0 : y));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            fieldX.text("");
            fieldY.text("");
        } else {
            Tuple tuple = (Tuple) value.getValue();
            try {
                fieldX.text(Integer.toString(tuple.getX()));
                fieldY.text(Integer.toString(tuple.getY()));
            } catch (Exception e) {
                fieldX.text("");
                fieldY.text("");
            }
        }
    }
}
