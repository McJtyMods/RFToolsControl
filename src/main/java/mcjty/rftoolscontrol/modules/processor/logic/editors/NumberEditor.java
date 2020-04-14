package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import static mcjty.lib.gui.widgets.Widgets.vertical;

public class NumberEditor extends AbstractParameterEditor {

    private ChoiceLabel typeLabel;

    private TextField field;

    @Override
    public int getHeight() {
        return 40;
    }


    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = vertical();

        typeLabel = new ChoiceLabel().choices("Integer", "Long", "Float", "Double")
                .event((newChoice) -> callback.valueChanged(readValue()))
                .desiredWidth(60);

        field = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());

        constantPanel.children(field, createLabeledPanel(mc, gui, "Type:", typeLabel, "Type of the number"));

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_NUMBER);
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(field);
    }

    @Override
    protected ParameterValue readConstantValue() {
        String choice = typeLabel.getCurrentChoice().toLowerCase();
        if (choice.startsWith("i")) {
            return ParameterValue.constant(parseIntSafe(field.getText()));
        } else if (choice.startsWith("l")) {
            return ParameterValue.constant(parseLongSafe(field.getText()));
        } else if (choice.startsWith("f")) {
            return ParameterValue.constant(parseFloatSafe(field.getText()));
        } else if (choice.startsWith("d")) {
            return ParameterValue.constant(parseDoubleSafe(field.getText()));
        } else {
            return ParameterValue.constant(0);
        }
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            field.text("");
        } else {
            Object v = value.getValue();
            if (v instanceof Integer || v == null) {
                typeLabel.choice("Integer");
            } else if (v instanceof Long) {
                typeLabel.choice("Long");
            } else if (v instanceof Float) {
                typeLabel.choice("Float");
            } else if (v instanceof Double) {
                typeLabel.choice("Double");
            }
            String s = TypeConverters.castNumberToString(v);
            field.text(s);
        }
    }

}
