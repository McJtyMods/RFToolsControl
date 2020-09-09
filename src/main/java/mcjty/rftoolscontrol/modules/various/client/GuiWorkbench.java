package mcjty.rftoolscontrol.modules.various.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchContainer;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

import static mcjty.lib.gui.widgets.Widgets.positional;


public class GuiWorkbench extends GenericGuiContainer<WorkbenchTileEntity, WorkbenchContainer> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/workbench.png");

    public GuiWorkbench(WorkbenchTileEntity te, WorkbenchContainer container, PlayerInventory inventory) {
        super(te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/ ManualEntry.EMPTY);

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    public static void register() {
        register(VariousModule.WORKBENCH_CONTAINER.get(), GuiWorkbench::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = positional().background(mainBackground);

        toplevel.bounds(guiLeft, guiTop, WIDTH, HEIGHT);
        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
