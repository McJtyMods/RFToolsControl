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
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
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
        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_FLUID);
    }

    @Override
    protected ParameterValue readConstantValue() {
        Object renderItem = blockRender.getRenderItem();
        if (renderItem instanceof ItemStack) {
            ItemStack stack = (ItemStack) renderItem;
            if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler.getTankProperties() != null && handler.getTankProperties().length > 0) {
                    if (handler.getTankProperties()[0] != null) {
                        return ParameterValue.constant(handler.getTankProperties()[0].getContents());
                    }
                }
            }
        }
        return ParameterValue.constant(null);
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            blockRender.setRenderItem(null);
        } else {
            FluidStack inv = (FluidStack) value.getValue();
            ItemStack itemStack = FluidContainerRegistry.fillFluidContainer(inv, new ItemStack(Items.BUCKET));
            if (itemStack != null) {
                // bucket not supported
                itemStack = new ItemStack(inv.getFluid().getBlock());
            }
            blockRender.setRenderItem(itemStack);
//            blockRender.setRenderItem(inv);
        }
    }
}
