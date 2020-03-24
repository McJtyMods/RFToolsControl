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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

public class FluidEditor extends AbstractParameterEditor {

    private BlockRender blockRender;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
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
                ItemStack holding = Minecraft.getInstance().player.inventory.getItemStack();
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

    @Nonnull
    private FluidStack stackToFluid(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> {
            if (handler.getTanks() > 0) {
                return handler.getFluidInTank(0);
            } else {
                return FluidStack.EMPTY;
            }
        }).orElseGet(() -> stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).map(handler -> {
            if (handler.getTanks() > 0) {
                return handler.getFluidInTank(0);
            } else {
                return FluidStack.EMPTY;
            }
        }).orElse(FluidStack.EMPTY));
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
