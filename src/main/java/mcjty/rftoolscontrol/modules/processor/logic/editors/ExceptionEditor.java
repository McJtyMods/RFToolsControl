package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class ExceptionEditor extends AbstractParameterEditor {

    private ChoiceLabel label;

    private static ExceptionType parseFacingSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return ExceptionType.getExceptionForCode(t);
    }

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();

        label = new ChoiceLabel()
                .desiredWidth(160);
        label.choices("*");
        for (ExceptionType exception : ExceptionType.values()) {
            label.choices(exception.getCode());
        }
        label.event((newChoice) -> callback.valueChanged(readValue()));

        constantPanel.children(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_EXCEPTION);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseFacingSafe(label.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            label.choice("*");
        } else {
            ExceptionType exception = (ExceptionType) value.getValue();
            label.choice(exception.getCode());
        }
    }
}
