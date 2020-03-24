package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.logic.TypeConverters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class NumberEditor extends AbstractParameterEditor {

    private ChoiceLabel typeLabel;

    private TextField field;

    @Override
    public int getHeight() {
        return 40;
    }


    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new VerticalLayout());

        typeLabel = new ChoiceLabel(mc, gui).addChoices("Integer", "Long", "Float", "Double")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(createLabeledPanel(mc, gui, "Type:", typeLabel, "Type of the number"));

        field = new TextField(mc, gui)
                .addTextEvent((parent, newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((parent, newText) -> closeWindow());
        constantPanel.addChild(field);

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
            field.setText("");
        } else {
            Object v = value.getValue();
            if (v instanceof Integer || v == null) {
                typeLabel.setChoice("Integer");
            } else if (v instanceof Long) {
                typeLabel.setChoice("Long");
            } else if (v instanceof Float) {
                typeLabel.setChoice("Float");
            } else if (v instanceof Double) {
                typeLabel.setChoice("Double");
            }
            String s = TypeConverters.castNumberToString(v);
            field.setText(s);
        }
    }

}
