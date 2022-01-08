package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collections;

import static mcjty.lib.gui.widgets.Widgets.horizontal;
import static mcjty.lib.gui.widgets.Widgets.label;

public class VectorEditor extends AbstractParameterEditor {

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();
        constantPanel.children(label("No constant editor"));

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
