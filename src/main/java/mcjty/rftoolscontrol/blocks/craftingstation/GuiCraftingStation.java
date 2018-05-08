package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.gui.GuiTools;
import mcjty.rftoolscontrol.network.PacketGetCraftableItems;
import mcjty.rftoolscontrol.network.PacketGetRequests;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity.*;

public class GuiCraftingStation extends GenericGuiContainer<CraftingStationTileEntity> {

    public static final int WIDTH = 231;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingstation.png");

    private WidgetList recipeList;
    private WidgetList requestList;
    private TextField searchField;
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
        searchField = new TextField(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 5, WIDTH-46-10, 16));
        cancelButton = new Button(mc, this)
//                .setLayoutHint(new PositionalLayout.PositionalHint(180, 5, 46, 16))
                .setLayoutHint(new PositionalLayout.PositionalHint(WIDTH-46-5, 5, 46, 16))
                .setText("Cancel")
                .setTooltips(TextFormatting.YELLOW + "Cancel request", "Cancel the currently selected", "crafting request")
                .addButtonEvent((widget -> cancelRequest()));
        toplevel.addChild(cancelButton).addChild(searchField);
    }

    private void cancelRequest() {
        int selected = requestList.getSelected();
        if (selected == -1) {
            return;
        }
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_CANCEL,
                TypedMap.builder()
                        .put(PARAM_INDEX, selected)
                        .build());
    }

    private void initRecipeList(Panel toplevel) {
        recipeList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 23, 120, 128)).setPropagateEventsToChildren(true)
                .setInvisibleSelection(true);
        Slider slider = new Slider(mc, this).setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(126, 23, 9, 128));
        toplevel.addChild(recipeList).addChild(slider);
    }

    private void initProgressList(Panel toplevel) {
        requestList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(136, 23, 80, 128));
        Slider slider = new Slider(mc, this).setScrollable(requestList).setLayoutHint(new PositionalLayout.PositionalHint(136+80+1, 23, 9, 128));
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
            final ItemStack stack = request.getStack();

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredWidth(16);
            requestList.addChild(panel);
            BlockRender blockRender = new BlockRender(mc, this) {
                @Override
                public List<String> getTooltips() {
                    ITooltipFlag flag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
                    List<String> list = stack.getTooltip(this.mc.player, flag);

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
                    .setRenderItem(stack)
                    .setOffsetX(-1)
                    .setOffsetY(-1);
            panel.addChild(blockRender);
            boolean failed = request.getFailed() != -1;
            boolean ok = request.getOk() != -1;
            panel.addChild(new Label(mc, this)
                    .setColor(failed ? 0xffff3030 : (ok ? 0xff30ff30 : StyleConfig.colorTextNormal))
                    .setText(failed ? "Failed!" : (ok ? "Ok" : "Wait (" + request.getTodo() + ")")));
        }
    }

    private void updateRecipeList() {
        String filterText = searchField.getText().toLowerCase().trim();

        fromServer_craftables.sort((r1, r2) -> {
            return r1.getDisplayName().compareTo(r2.getDisplayName());
        });

        recipeList.removeChildren();
        Panel panel = null;
        int index = 0;
        for (ItemStack stack : fromServer_craftables) {
            String displayName = stack.getDisplayName();
            if ((!filterText.isEmpty()) && !displayName.toLowerCase().contains(filterText)) {
                continue;
            }

            if (panel == null || panel.getChildCount() >= 6) {
                panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(3).setHorizontalMargin(1))
                        .setDesiredHeight(16);
                recipeList.addChild(panel);
            }
            BlockRender blockRender = new BlockRender(mc, this) {
                @Override
                public List<String> getTooltips() {
                    ITooltipFlag flag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
                    List<String> list = stack.getTooltip(this.mc.player, flag);

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
                        if (shift) {
                            askAmountToCraft(stack);
                        } else {
                            requestItem(stack, 1);
                        }
                    }
                }

                @Override
                public void doubleClick(Widget widget) {
                }
            });
            panel.addChild(blockRender);
        }
    }

    private Integer safeParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void askAmountToCraft(ItemStack stack) {
        GuiTools.askSomething(mc, this, getWindowManager(), 220, 50, "Craft amount:", "", s -> {
            Integer a = safeParse(s);
            if (a != null) {
                requestItem(stack, a);
            }
        });
    }

    private void requestItem(ItemStack stack, int amount) {
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_REQUEST,
                TypedMap.builder()
                        .put(PARAM_ITEMNAME, stack.getItem().getRegistryName().toString())
                        .put(PARAM_META, stack.getItemDamage())
                        .put(PARAM_NBT, stack.hasTagCompound() ? stack.serializeNBT().toString() : "")
                        .put(PARAM_AMOUNT, amount)
                        .build());
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
