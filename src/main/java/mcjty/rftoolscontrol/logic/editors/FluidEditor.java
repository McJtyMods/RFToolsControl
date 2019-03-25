package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidEditor extends AbstractParameterEditor {

    private BlockRender blockRender;

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());

        Label label = new Label(mc, gui).setText("Drop bucket:");
        constantPanel.addChild(label);

        blockRender = new BlockRender(mc, gui)
                .setDesiredWidth(18+100).setDesiredHeight(18).setFilledRectThickness(1).setFilledBackground(0xff555555)
                .setShowLabel(true);

        constantPanel.addChild(blockRender);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget widget) {
                ItemStack holding = Minecraft.getMinecraft().player.inventory.getItemStack();
                if (holding.isEmpty()) {
                    blockRender.setRenderItem(null);
                } else {
                    blockRender.setRenderItem(stackToFluid(holding));
                }
                callback.valueChanged(readValue());
            }

            @Override
            public void doubleClick(Widget widget) {
            }
        });
        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_FLUID);
    }

    @Override
    protected ParameterValue readConstantValue() {
        Object renderItem = blockRender.getRenderItem();
        if (renderItem instanceof ItemStack) {
            FluidStack fluidStack = stackToFluid((ItemStack) renderItem);
            return ParameterValue.constant(fluidStack);
        } else if (renderItem instanceof FluidStack) {
            FluidStack stack = (FluidStack) renderItem;
            return ParameterValue.constant(stack);
        }
        return ParameterValue.constant(null);
    }

    private FluidStack stackToFluid(ItemStack stack) {
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler.getTankProperties() != null && handler.getTankProperties().length > 0) {
                if (handler.getTankProperties()[0] != null) {
                    return handler.getTankProperties()[0].getContents();
                }
            }
        }

        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (handler.getTankProperties() != null && handler.getTankProperties().length > 0) {
                if (handler.getTankProperties()[0] != null) {
                    return handler.getTankProperties()[0].getContents();
                }
            }
        }

        return null;
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            blockRender.setRenderItem(null);
        } else {
            FluidStack inv = (FluidStack) value.getValue();
            blockRender.setRenderItem(inv);
        }
    }
}
