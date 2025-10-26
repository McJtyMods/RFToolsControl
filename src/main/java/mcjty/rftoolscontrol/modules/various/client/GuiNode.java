package mcjty.rftoolscontrol.modules.various.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ClientTools;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiNode extends GenericGuiContainer<NodeTileEntity, GenericContainer> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 30;

    public GuiNode(GenericContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/ ManualEntry.EMPTY);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(VariousModule.NODE_CONTAINER.get(), GuiNode::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = vertical().filledRectThickness(2);

        TextField channelField = new TextField().tooltips("Set the name of the network", "channel to connect too");
        channelField.name("channel");

        TextField nodeNameField = new TextField().tooltips("Set the name of this node");
        nodeNameField.name("node");

        Panel bottomPanel = horizontal().
                children(label("Channel:"), channelField, label("Node:"), nodeNameField);
        toplevel.children(bottomPanel);

        toplevel.bounds(leftPos, topPos, WIDTH, HEIGHT);
        window = new Window(this, toplevel);

        GenericTileEntity be = getBE();
        window.bind("channel", be, "channel");
        window.bind("node", be, "node");

        ClientTools.enableKeyboardRepeat();
    }
}
