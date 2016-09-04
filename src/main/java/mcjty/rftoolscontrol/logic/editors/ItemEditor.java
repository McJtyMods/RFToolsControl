package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;

public class ItemEditor extends AbstractParameterEditor {

    private BlockRender blockRender;

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());

        Label label = new Label(mc, gui).setText("Drop item:");
        constantPanel.addChild(label);

        blockRender = new BlockRender(mc, gui).setDesiredWidth(18).setDesiredHeight(18).setFilledRectThickness(1).setFilledBackground(0xff555555);
        constantPanel.addChild(blockRender);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget widget) {
                ItemStack holding = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
                if (holding == null) {
                    blockRender.setRenderItem(null);
                } else {
                    ItemStack copy = holding.copy();
                    copy.stackSize = 1;
                    blockRender.setRenderItem(copy);
                }
                callback.valueChanged(readValue());
            }

            @Override
            public void doubleClick(Widget widget) {
            }
        });
        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_ITEM);
    }

    @Override
    protected ParameterValue readConstantValue() {
        return ParameterValue.constant(blockRender.getRenderItem());
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            blockRender.setRenderItem(null);
        } else {
            ItemStack inv = (ItemStack) value.getValue();
            blockRender.setRenderItem(inv);
        }
    }
}
