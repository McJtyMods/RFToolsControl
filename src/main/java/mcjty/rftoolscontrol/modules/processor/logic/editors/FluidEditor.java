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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.horizontal;

public class FluidEditor extends AbstractParameterEditor {

    private BlockRender blockRender;

    @Override
    public void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = horizontal();

        Label label = Widgets.label("Drop bucket:");
        constantPanel.children(label);

        blockRender = new BlockRender()
                .desiredWidth(18+100).desiredHeight(18).filledRectThickness(1).filledBackground(0xff555555)
                .showLabel(true);

        constantPanel.children(blockRender);
        blockRender.event(new BlockRenderEvent() {
            @Override
            public void select() {
                ItemStack holding = Minecraft.getInstance().player.inventory.getCarried();
                if (holding.isEmpty()) {
                    blockRender.renderItem(null);
                } else {
                    blockRender.renderItem(stackToFluid(holding));
                }
                callback.valueChanged(readValue());
            }

            @Override
            public void doubleClick() {
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
            blockRender.renderItem(null);
        } else {
            FluidStack inv = (FluidStack) value.getValue();
            blockRender.renderItem(inv);
        }
    }
}
