package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.StringUtils;

import static mcjty.lib.gui.widgets.Widgets.vertical;

public class InventoryEditor extends AbstractParameterEditor {

    private TextField nameLabel;
    private ChoiceLabel sideLabel;
    private ChoiceLabel intSideLabel;

    private static Inventory parseInventorySafe(String name, String sideS, String intSideS) {
        Direction side;
        if ("*".equals(sideS)) {
            return null;
        } else {
            side = Direction.byName(StringUtils.lowerCase(sideS));
        }
        Direction intSide;
        if ("*".equals(intSideS)) {
            intSide = null;
        } else {
            intSide = Direction.byName(StringUtils.lowerCase(intSideS));
        }
        return new Inventory(name, side, intSide);
    }

    @Override
    public void initialFocusInternal(Window window) {
        window.setTextFocus(nameLabel);
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = vertical();
        nameLabel = new TextField()
                .event((text) -> callback.valueChanged(readValue()))
                .addTextEnterEvent((newText) -> closeWindow())
                .desiredWidth(50).desiredHeight(14);
        constantPanel.children(createLabeledPanel(mc, gui, "Node name:", nameLabel, "Optional name of a node in the network"));
        sideLabel = new ChoiceLabel().choices("*", "Down", "Up", "North", "South", "West", "East")
                .event((newChoice) -> callback.valueChanged(readValue()))
                .desiredWidth(60);
        constantPanel.children(createLabeledPanel(mc, gui, "Side:", sideLabel, "Side relative to processor or node", "for the desired inventory"));
        intSideLabel = new ChoiceLabel().choices("*", "Down", "Up", "North", "South", "West", "East")
                .event((newChoice) -> callback.valueChanged(readValue()))
                .desiredWidth(60);
        constantPanel.children(createLabeledPanel(mc, gui, "Access:", intSideLabel, "Optional side from which we want to", "access the given inventory"));

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_INVENTORY);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseInventorySafe(nameLabel.getText(), sideLabel.getCurrentChoice(), intSideLabel.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            sideLabel.choice("*");
        } else {
            Inventory inv = (Inventory) value.getValue();
            nameLabel.text(inv.getNodeNameSafe());
            sideLabel.choice(StringUtils.capitalize(inv.getSide().toString()));
            if (inv.getIntSide() == null) {
                intSideLabel.choice("*");
            } else {
                intSideLabel.choice(StringUtils.capitalize(inv.getIntSide().toString()));
            }
        }
    }
}
