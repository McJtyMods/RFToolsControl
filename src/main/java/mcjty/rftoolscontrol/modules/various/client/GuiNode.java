package mcjty.rftoolscontrol.modules.various.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.blocks.NodeContainer;
import mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity.PARAM_CHANNEL;
import static mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity.PARAM_NODE;

public class GuiNode extends GenericGuiContainer<NodeTileEntity, NodeContainer> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 30;

    private TextField channelField;
    private TextField nodeNameField;

    public GuiNode(NodeTileEntity te, NodeContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/ ManualEntry.EMPTY);

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = vertical().filledRectThickness(2);

        channelField = new TextField().tooltips("Set the name of the network", "channel to connect too").event((newText) -> updateNode());
        channelField.text(tileEntity.getChannelName());

        nodeNameField = new TextField().tooltips("Set the name of this node").event((newText) -> updateNode());
        nodeNameField.text(tileEntity.getNodeName());

        Panel bottomPanel = horizontal().
                children(label("Channel:"), channelField, label("Node:"), nodeNameField);
        toplevel.children(bottomPanel);

        toplevel.bounds(guiLeft, guiTop, WIDTH, HEIGHT);
        window = new Window(this, toplevel);

        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    private void updateNode() {
        sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, NodeTileEntity.CMD_UPDATE,
                TypedMap.builder()
                        .put(PARAM_NODE, nodeNameField.getText())
                        .put(PARAM_CHANNEL, channelField.getText())
                        .build());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow(xxx);
    }
}
