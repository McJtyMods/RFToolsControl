package mcjty.rftoolscontrol.modules.various.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchContainer;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiWorkbench extends GenericGuiContainer<WorkbenchTileEntity, WorkbenchContainer> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/workbench.png");

    public GuiWorkbench(WorkbenchTileEntity te, WorkbenchContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/0, "workbench");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setLayout(new PositionalLayout()).setBackground(mainBackground);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
