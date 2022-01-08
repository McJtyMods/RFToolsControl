package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class IntegerEditor extends AbstractParameterEditor {

    private TextField field;
    private ToggleButton hexMode;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        field = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());
        hexMode = new ToggleButton()
                .event(this::updateHex)
                .checkMarker(true)
                .text("Hex");
        constantPanel.children(field, hexMode);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_INTEGER);
    }

    private void updateHex() {
        String value = field.getText();
        if (hexMode.isPressed()) {
            if (!value.startsWith("$")) {
                try {
                    int i = Integer.parseInt(value);
                    value = "$" + Integer.toHexString(i);
                    field.text(value);
                } catch (NumberFormatException e) {
                }
            }
        } else {
            if (value.startsWith("$")) {
                Integer i = parseIntSafe(value);
                if (i != null) {
                    field.text(String.valueOf(i));
                }
            }
        }
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(field);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseIntSafe(field.getText()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            field.text("");
        } else {
            try {
                field.text(Integer.toString((Integer) value.getValue()));
            } catch (Exception e) {
                field.text("");
            }
        }
        updateHex();
    }
}
