package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class LongEditor extends AbstractParameterEditor {

    private TextField field;
    private ToggleButton hexMode;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        field = new TextField()
                .event((newText) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow());
        constantPanel.children(field);
        hexMode = new ToggleButton()
                .event(this::updateHex)
                .checkMarker(true)
                .text("Hex");
        constantPanel.children(hexMode);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_LONG);
    }

    private void updateHex() {
        String value = field.getText();
        if (hexMode.isPressed()) {
            if (!value.startsWith("$")) {
                try {
                    long i = Long.parseLong(value);
                    value = "$" + Long.toHexString(i);
                    field.text(value);
                } catch (NumberFormatException e) {
                }
            }
        } else {
            if (value.startsWith("$")) {
                Long i = parseLongSafe(value);
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
        return ParameterValue.constant(parseLongSafe(field.getText()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            field.text("");
        } else {
            try {
                field.text(Long.toString((Long) value.getValue()));
            } catch (Exception e) {
                field.text("");
            }
        }
        updateHex();
    }
}
