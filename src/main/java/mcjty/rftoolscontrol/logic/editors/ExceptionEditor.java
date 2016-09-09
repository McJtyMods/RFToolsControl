package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.blocks.processor.ProgException;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class ExceptionEditor extends AbstractParameterEditor {

    private ChoiceLabel label;

    private static ProgException parseFacingSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return ProgException.getExceptionForCode(t);
    }

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());

        label = new ChoiceLabel(mc, gui)
                .setDesiredWidth(160);
        label.addChoices("*");
        for (ProgException exception : ProgException.values()) {
            label.addChoices(exception.getCode());
        }
        label.addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()));

        constantPanel.addChild(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_EXCEPTION);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseFacingSafe(label.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            label.setChoice("*");
        } else {
            ProgException exception = (ProgException) value.getValue();
            label.setChoice(exception.getCode());
        }
    }
}
