package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class ItemEditor extends AbstractParameterEditor {

    private BlockRender blockRender;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();

        Label label = Widgets.label("Drop item:");
        blockRender = new BlockRender()
                .desiredWidth(18+100).desiredHeight(18).filledRectThickness(1).filledBackground(0xff555555)
                .showLabel(true);

        constantPanel.children(label, blockRender);

        blockRender.event(new BlockRenderEvent() {
            @Override
            public void select() {
                ItemStack holding = Minecraft.getInstance().player.inventory.getCarried();
                if (holding.isEmpty()) {
                    blockRender.renderItem(null);
                } else {
                    ItemStack copy = holding.copy();
                    copy.setCount(1);
                    blockRender.renderItem(copy);
                }
                callback.valueChanged(readValue());
            }

            @Override
            public void doubleClick() {
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
            blockRender.renderItem(null);
        } else {
            ItemStack inv = (ItemStack) value.getValue();
            blockRender.renderItem(inv);
        }
    }
}
