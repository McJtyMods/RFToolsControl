package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiCraftingCard extends GenericGuiContainer {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 188;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingcard.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsControl.MODID, "textures/gui/guielements.png");

    public GuiCraftingCard(CraftingCardContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, null, container, RFToolsControl.GUI_MANUAL_CONTROL, "craftingcard");
        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
