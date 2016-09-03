package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

public class InventoryEditor extends AbstractParameterEditor {

    private TextField nameLabel;
    private ChoiceLabel sideLabel;
    private ChoiceLabel intSideLabel;

    private static Inventory parseInventorySafe(String name, String sideS, String intSideS) {
        EnumFacing side;
        if ("*".equals(sideS)) {
            side = EnumFacing.UP;
        } else {
            side = EnumFacing.byName(StringUtils.lowerCase(sideS));
        }
        EnumFacing intSide;
        if ("*".equals(intSideS)) {
            intSide = null;
        } else {
            intSide = EnumFacing.byName(StringUtils.lowerCase(intSideS));
        }
        return new Inventory(name, side, intSide);
    }

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        nameLabel = new TextField(mc, gui)
                .addTextEnterEvent((o,text) -> callback.valueChanged(readValue()))
                .setDesiredWidth(40);
        constantPanel.addChild(nameLabel);
        sideLabel = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(sideLabel);
        intSideLabel = new ChoiceLabel(mc, gui).addChoices("*", "Down", "Up", "North", "South", "West", "East")
                .addChoiceEvent((parent, newChoice) -> callback.valueChanged(readValue()))
                .setDesiredWidth(60);
        constantPanel.addChild(intSideLabel);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_INVENTORY);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(parseInventorySafe(nameLabel.getText(), sideLabel.getCurrentChoice(), intSideLabel.getCurrentChoice()));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            sideLabel.setChoice("*");
        } else {
            Inventory inv = (Inventory) value.getValue();
            nameLabel.setText(inv.getNodeName() == null ? "" : inv.getNodeName());
            sideLabel.setChoice(StringUtils.capitalize(inv.getSide().toString()));
            if (inv.getIntSide() == null) {
                intSideLabel.setChoice("*");
            } else {
                intSideLabel.setChoice(StringUtils.capitalize(inv.getIntSide().toString()));
            }
        }
    }
}
