package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

public class SideEditor extends AbstractParameterEditor {

    private ChoiceLabel label;

    private static EnumFacing parseFacingSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return EnumFacing.byName(StringUtils.lowerCase(t));
    }

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        label = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel);
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
            String choice = StringUtils.capitalize(value.getValue().toString());
            label.setChoice(choice);
        }
    }
}
