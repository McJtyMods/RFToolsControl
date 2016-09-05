package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

public class SideEditor extends AbstractParameterEditor {

    private TextField nameLabel;
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

        nameLabel = new TextField(mc, gui)
                .addTextEvent((o,text) -> callback.valueChanged(readValue()))
                .setDesiredWidth(40);
        constantPanel.addChild(nameLabel);

        label = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(label);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_SIDE);
    }

    @Override
    protected ParameterValue readConstantValue() {
        BlockSide side = new BlockSide(nameLabel.getText(), parseFacingSafe(label.getCurrentChoice()));
        return ParameterValue.constant(side);
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            nameLabel.setText("");
            label.setChoice("*");
        } else {
            BlockSide side = (BlockSide) value.getValue();
            nameLabel.setText(side.getNodeName() == null ? "" : side.getNodeName());
            String choice = StringUtils.capitalize(side.toString());
            label.setChoice(choice);
        }
    }
}
