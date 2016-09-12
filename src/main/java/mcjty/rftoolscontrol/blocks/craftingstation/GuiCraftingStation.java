package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.PacketGetCraftableItems;
import mcjty.rftoolscontrol.network.PacketGetRequests;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiCraftingStation extends GenericGuiContainer<CraftingStationTileEntity> {

    public static final int WIDTH = 231;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingstation.png");

    private WidgetList recipeList;
    private WidgetList requestList;
    private Button cancelButton;

    private static List<ItemStack> fromServer_craftables = new ArrayList<>();
    public static void storeCraftableForClient(List<ItemStack> items) {
        fromServer_craftables = new ArrayList<>(items);
    }

    private static List<CraftingRequest> fromServer_requests = new ArrayList<>();
    public static void storeRequestsForClient(List<CraftingRequest> requests) {
        fromServer_requests = new ArrayList<>(requests);
    }

    private int listDirty = 0;

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
        initButtons(toplevel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);
    }

    private void initButtons(Panel toplevel) {
        cancelButton = new Button(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(180, 5, 46, 16))
                .setText("Cancel")
                .setTooltips("Cancel the currently selected", "crafting request")
                .addButtonEvent((widget -> cancelRequest()));
        toplevel.addChild(cancelButton);
    }

    private void cancelRequest() {
        int selected = requestList.getSelected();
        if (selected == -1) {
            return;
        }
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_CANCEL,
                new Argument("index", selected));
    }

    private void initRecipeList(Panel toplevel) {
        recipeList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 70, 146)).setPropagateEventsToChildren(true)
                .setInvisibleSelection(true);
        Slider slider = new Slider(mc, this).setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(76, 5, 9, 146));
        toplevel.addChild(recipeList).addChild(slider);
    }

    private void initProgressList(Panel toplevel) {
        requestList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(86, 5, 80, 146));
        Slider slider = new Slider(mc, this).setScrollable(requestList).setLayoutHint(new PositionalLayout.PositionalHint(86+80+1, 5, 9, 146));
        toplevel.addChild(requestList).addChild(slider);
    }

    private void requestLists() {
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetCraftableItems(tileEntity.getPos()));
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetRequests(tileEntity.getPos()));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestLists();
            listDirty = 10;
        }
    }

    private void updateRequestList() {
        requestList.removeChildren();
        for (CraftingRequest request : fromServer_requests) {
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredWidth(16);
            requestList.addChild(panel);
            BlockRender blockRender = new BlockRender(mc, this) {
                @Override
                public List<String> getTooltips() {
                    ItemStack stack = request.getStack();
                    List<String> list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

                    for (int i = 0; i < list.size(); ++i) {
                        if (i == 0) {
                            list.set(i, stack.getRarity().rarityColor + list.get(i));
                        } else {
                            list.set(i, TextFormatting.GRAY + list.get(i));
                        }
                    }

                    return list;
                }
            }
                    .setRenderItem(request.getStack())
                    .setOffsetX(-1)
                    .setOffsetY(-1);
            panel.addChild(blockRender);
            panel.addChild(new Label(mc, this).setText("Waiting..."));
        }
    }

    private void updateRecipeList() {
        recipeList.removeChildren();
        Panel panel = null;
        int index = 0;
        for (ItemStack stack : fromServer_craftables) {
            if (panel == null || panel.getChildCount() >= 3) {
                panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(3).setHorizontalMargin(1))
                        .setDesiredHeight(16);
                recipeList.addChild(panel);
            }
            BlockRender blockRender = new BlockRender(mc, this) {
                @Override
                public List<String> getTooltips() {
                    List<String> list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

                    for (int i = 0; i < list.size(); ++i) {
                        if (i == 0) {
                            list.set(i, stack.getRarity().rarityColor + list.get(i));
                        } else {
                            list.set(i, TextFormatting.GRAY + list.get(i));
                        }
                    }

                    List<String> newlist = new ArrayList<>();
                    newlist.add(TextFormatting.GREEN + "Click: "+ TextFormatting.WHITE + "craft single");
                    newlist.add(TextFormatting.GREEN + "Shift + click: "+ TextFormatting.WHITE + "craft amount");
                    newlist.add("");
                    newlist.addAll(list);
                    return newlist;
                }
            }
                    .setRenderItem(stack)
                    .setHilightOnHover(true)
                    .setOffsetX(-1)
                    .setOffsetY(-1)
                    .setUserObject(index);
            index++;
            blockRender.addSelectionEvent(new BlockRenderEvent() {
                @Override
                public void select(Widget widget) {
                    BlockRender br = (BlockRender) widget;
                    Object item = br.getRenderItem();
                    if (item != null) {
                        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        Object index = br.getUserObject();
                        requestItem((Integer) index, shift ? 1 : -1);
                    }
                }

                @Override
                public void doubleClick(Widget widget) {
                }
            });
            panel.addChild(blockRender);
        }
    }

    private void requestItem(int index, int amount) {
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_REQUEST,
                new Argument("index", index),
                new Argument("amount", amount));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        requestListsIfNeeded();
        updateRecipeList();
        updateRequestList();
        if (requestList.getSelected() >= requestList.getChildCount()) {
            requestList.setSelected(-1);
        }
        cancelButton.setEnabled(requestList.getSelected() != -1);
        drawWindow();
    }
}
