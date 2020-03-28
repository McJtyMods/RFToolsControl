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
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;

import java.awt.*;

import static mcjty.rftoolscontrol.blocks.node.NodeTileEntity.PARAM_CHANNEL;
import static mcjty.rftoolscontrol.blocks.node.NodeTileEntity.PARAM_NODE;

public class GuiNode extends GenericGuiContainer<NodeTileEntity, NodeContainer> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 30;

    private TextField channelField;
    private TextField nodeNameField;

    public GuiNode(NodeTileEntity te, NodeContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/0, "networking");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        channelField = new TextField(minecraft, this).setTooltips("Set the name of the network", "channel to connect too").addTextEvent((parent, newText) -> updateNode());
        channelField.setText(tileEntity.getChannelName());

        nodeNameField = new TextField(minecraft, this).setTooltips("Set the name of this node").addTextEvent((parent, newText) -> updateNode());
        nodeNameField.setText(tileEntity.getNodeName());

        Panel bottomPanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).
                addChild(new Label(minecraft, this).setText("Channel:")).addChild(channelField).
                addChild(new Label(minecraft, this).setText("Node:")).addChild(nodeNameField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);

        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    private void updateNode() {
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, RFToolsControl.MODID, NodeTileEntity.CMD_UPDATE,
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
