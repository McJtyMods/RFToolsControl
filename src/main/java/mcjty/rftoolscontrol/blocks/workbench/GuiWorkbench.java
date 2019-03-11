package mcjty.rftoolscontrol.blocks.workbench;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.proxy.GuiProxy;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiWorkbench extends GenericGuiContainer<WorkbenchTileEntity> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/workbench.png");

    public GuiWorkbench(WorkbenchTileEntity tileEntity, WorkbenchContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, GuiProxy.GUI_MANUAL_CONTROL, "workbench");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
