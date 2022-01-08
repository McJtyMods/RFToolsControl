package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.BlockSide;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.StringUtils;

import static mcjty.lib.gui.widgets.Widgets.vertical;

public class SideEditor extends AbstractParameterEditor {

    private TextField nameLabel;
    private ChoiceLabel label;

    private static Direction parseFacingSafe(String t) {
        if ("*".equals(t)) {
            return null;
        }
        return Direction.byName(StringUtils.lowerCase(t));
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(nameLabel);
    }

    @Override
    public int getHeight() {
        return 35;
    }

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = vertical();

        nameLabel = new TextField()
                .event((text) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow())
                .desiredWidth(50).desiredHeight(14);
        constantPanel.children(createLabeledPanel(mc, gui, "Node name:", nameLabel, "Optional name of a node in the network"));

        label = new ChoiceLabel().choices("*", "Down", "Up", "North", "South", "West", "East")
                .event((newChoice) -> callback.valueChanged(readValue()))
                .desiredWidth(60);
        constantPanel.children(createLabeledPanel(mc, gui, "Side:", label, "Side relative to processor or node", "for the desired block"));

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
            nameLabel.text("");
            label.choice("*");
        } else {
            BlockSide side = (BlockSide) value.getValue();
            nameLabel.text(side.getNodeNameSafe());
            String choice = StringUtils.capitalize(side.toString());
            label.choice(choice);
        }
    }
}
