package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiCraftingStation extends GenericGuiContainer<CraftingStationTileEntity> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingstation.png");

    private WidgetList recipeList;
    private WidgetList progressList;

    public GuiCraftingStation(CraftingStationTileEntity tileEntity, CraftingStationContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, RFToolsControl.GUI_MANUAL_CONTROL, "craftingstation");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground);

        initRecipeList(toplevel);
        initProgressList(toplevel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);
    }

    private void initRecipeList(Panel toplevel) {
        recipeList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 70, 128));
        Slider slider = new Slider(mc, this).setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(76, 5, 9, 128));
        toplevel.addChild(recipeList).addChild(slider);
    }

    private void initProgressList(Panel toplevel) {
        progressList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(86, 5, 70, 128));
        Slider slider = new Slider(mc, this).setScrollable(progressList).setLayoutHint(new PositionalLayout.PositionalHint(86+70+1, 5, 9, 128));
        toplevel.addChild(progressList).addChild(slider);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
