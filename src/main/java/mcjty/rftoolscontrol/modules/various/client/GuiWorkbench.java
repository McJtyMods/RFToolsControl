package mcjty.rftoolscontrol.modules.various.client;

import javax.annotation.Nonnull;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchContainer;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static mcjty.lib.gui.widgets.Widgets.positional;


public class GuiWorkbench extends GenericGuiContainer<WorkbenchTileEntity, WorkbenchContainer> {

    public static final int WIDTH = 171;
    public static final int HEIGHT = 236;

    private static final Identifier mainBackground = Identifier.fromNamespaceAndPath(RFToolsControl.MODID, "textures/gui/workbench.png");

    public GuiWorkbench(WorkbenchContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, VariousModule.WORKBENCH.block().get().getManualEntry());

        setWindowDimensions(WIDTH, HEIGHT);
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(VariousModule.WORKBENCH_CONTAINER.get(), GuiWorkbench::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = positional().background(mainBackground);

        toplevel.bounds(leftPos, topPos, WIDTH, HEIGHT);
        window = new Window(this, toplevel);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
        drawWindow(graphics, partialTicks, mouseX, mouseY);
    }
}
