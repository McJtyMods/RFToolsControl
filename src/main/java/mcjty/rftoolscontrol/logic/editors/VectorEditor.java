package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.Collections;

public class VectorEditor extends AbstractParameterEditor {

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        constantPanel.addChild(new Label(mc, gui).setText("No constant editor"));

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_VECTOR);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(Collections.emptyList());
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
    }
}
