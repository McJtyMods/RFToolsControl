package mcjty.rftoolscontrol.blocks.node;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.setup.GuiProxy;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;

import java.awt.Rectangle;

import static mcjty.rftoolscontrol.blocks.node.NodeTileEntity.PARAM_CHANNEL;
import static mcjty.rftoolscontrol.blocks.node.NodeTileEntity.PARAM_NODE;

public class GuiNode extends GenericGuiContainer<NodeTileEntity> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 30;

    private TextField channelField;
    private TextField nodeNameField;

    public GuiNode(NodeTileEntity tileEntity, EmptyContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, GuiProxy.GUI_MANUAL_CONTROL, "networking");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        channelField = new TextField(mc, this).setTooltips("Set the name of the network", "channel to connect too").addTextEvent((parent, newText) -> updateNode());
        channelField.setText(tileEntity.getChannelName());

        nodeNameField = new TextField(mc, this).setTooltips("Set the name of this node").addTextEvent((parent, newText) -> updateNode());
        nodeNameField.setText(tileEntity.getNodeName());

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, this).setText("Channel:")).addChild(channelField).
                addChild(new Label(mc, this).setText("Node:")).addChild(nodeNameField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);
    }

    private void updateNode() {
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, NodeTileEntity.CMD_UPDATE,
                TypedMap.builder()
                        .put(PARAM_NODE, nodeNameField.getText())
                        .put(PARAM_CHANNEL, channelField.getText())
                        .build());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
