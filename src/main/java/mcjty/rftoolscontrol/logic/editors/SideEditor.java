package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.api.paremeters.ParameterType;
import mcjty.rftoolscontrol.api.paremeters.ParameterValue;
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
    public int getHeight() {
        return 35;
    }

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new VerticalLayout());

        nameLabel = new TextField(mc, gui)
                .addTextEvent((o,text) -> callback.valueChanged(readValue()))
                .setDesiredWidth(50).setDesiredHeight(14);
        constantPanel.addChild(createLabeledPanel(mc, gui, "Node name:", nameLabel, "Optional name of a node in the network"));

        label = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(createLabeledPanel(mc, gui, "Side:", label, "Side relative to processor or node", "for the desired block"));

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
