package mcjty.rftoolscontrol.modules.craftingstation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.lib.gui.GuiPopupTools;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity.*;

public class GuiCraftingStation extends GenericGuiContainer<CraftingStationTileEntity, GenericContainer> {

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

    public GuiCraftingStation(CraftingStationTileEntity te, GenericContainer container, Inventory inventory) {
        super(te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/ ManualEntry.EMPTY);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register() {
        register(CraftingStationModule.CRAFTING_STATION_CONTAINER.get(), GuiCraftingStation::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = Widgets.positional().background(mainBackground);

        initRecipeList(toplevel);
        initProgressList(toplevel);
        initButtons(toplevel);

        toplevel.bounds(leftPos, topPos, WIDTH, HEIGHT);
        window = new Window(this, toplevel);

        window.event("cancel", (source, params) -> cancelRequest());
    }

    private void initButtons(Panel toplevel) {
        searchField = textfield(5, 5, WIDTH-46-10, 16);
        cancelButton = button(WIDTH-46-5, 5, 46, 16, "Cancel")
                .channel("cancel")
                .tooltips(ChatFormatting.YELLOW + "Cancel request", "Cancel the currently selected", "crafting request");
        toplevel.children(cancelButton, searchField);
    }

    private void cancelRequest() {
        int selected = requestList.getSelected();
        if (selected == -1) {
            return;
        }
        sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_CANCEL,
                TypedMap.builder()
                        .put(PARAM_INDEX, selected)
                        .build());
    }

    private void initRecipeList(Panel toplevel) {
        recipeList = list(5, 23, 120, 128).name("recipes").propagateEventsToChildren(true)
                .invisibleSelection(true);
        Slider slider = slider(126, 23, 9, 128).scrollableName("recipes");
        toplevel.children(recipeList, slider);
    }

    private void initProgressList(Panel toplevel) {
        requestList = list(136, 23, 80, 128).name("requests");
        Slider slider = slider(136+80+1, 23, 9, 128).scrollableName("requests");
        toplevel.children(requestList, slider);
    }

    private void requestLists() {
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetListFromServer(tileEntity.getBlockPos(), CMD_GETCRAFTABLE.name()));
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetListFromServer(tileEntity.getBlockPos(), CMD_GETREQUESTS.name()));
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

            Panel panel = horizontal().desiredWidth(16);
            requestList.children(panel);
            BlockRender blockRender = new BlockRender()
                    .renderItem(stack)
                    .offsetX(-1)
                    .offsetY(-1);
            panel.children(blockRender);
            boolean failed = request.getFailed() != -1;
            boolean ok = request.getOk() != -1;
            if (failed) {
                panel.children(label("Failed!").color(0xffff3030));
            } else {
                panel.children(label(ok ? "Ok" : "Wait (" + request.getTodo() + ")")
                        .color(ok ? 0xff30ff30 : StyleConfig.colorTextNormal));
            }
        }
    }

    @Override
    protected List<Component> addCustomLines(List<Component> oldList, BlockRender blockRender, ItemStack stack) {
        if (blockRender.getUserObject() instanceof Integer) {
            List<Component> newlist = new ArrayList<>();
            newlist.add(new TextComponent("Click: ").withStyle(ChatFormatting.GREEN)
                    .append(new TextComponent("craft single").withStyle(ChatFormatting.WHITE)));
            newlist.add(new TextComponent("Shift + click: ").withStyle(ChatFormatting.GREEN)
                    .append(new TextComponent("craft amount").withStyle(ChatFormatting.WHITE)));
            newlist.add(new TextComponent(""));
            newlist.addAll(oldList);
            return newlist;
        } else {
            return oldList;
        }
    }


    private void updateRecipeList() {
        String filterText = searchField.getText().toLowerCase().trim();

        fromServer_craftables.sort(Comparator.comparing(r -> r.getHoverName().getString()));  // @todo getFormattedText

        recipeList.removeChildren();
        Panel panel = null;
        int index = 0;
        for (ItemStack stack : fromServer_craftables) {
            String displayName = stack.getHoverName().getString() /* was getFormattedText() */;
            if ((!filterText.isEmpty()) && !displayName.toLowerCase().contains(filterText)) {
                continue;
            }

            if (panel == null || panel.getChildCount() >= 6) {
                panel = horizontal(1, 3).desiredHeight(16);
                recipeList.children(panel);
            }
            BlockRender blockRender = new BlockRender()
                    .renderItem(stack)
                    .hilightOnHover(true)
                    .offsetX(-1)
                    .offsetY(-1)
                    .userObject(index);
            index++;
            blockRender.event(new BlockRenderEvent() {
                @Override
                public void select() {
                    Object item = blockRender.getRenderItem();
                    if (item != null) {
                        boolean shift = SafeClientTools.isSneaking();
                        Object index = blockRender.getUserObject();
                        if (shift) {
                            askAmountToCraft(stack);
                        } else {
                            requestItem(stack, 1);
                        }
                    }
                }

                @Override
                public void doubleClick() {
                }
            });
            panel.children(blockRender);
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
        GuiPopupTools.askSomething(minecraft, this, getWindowManager(), 220, 50, "Craft amount:", "", s -> {
            Integer a = safeParse(s);
            if (a != null) {
                requestItem(stack, a);
            }
        });
    }

    private void requestItem(ItemStack stack, int amount) {
        sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, CraftingStationTileEntity.CMD_REQUEST,
                TypedMap.builder()
                        .put(PARAM_ITEMNAME, stack.getItem().getRegistryName().toString())
                        .put(PARAM_NBT, stack.hasTag() ? stack.serializeNBT().toString() : "")
                        .put(PARAM_AMOUNT, amount)
                        .build());
    }

    @Override
    protected void renderBg(@Nonnull PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        requestListsIfNeeded();
        updateRecipeList();
        updateRequestList();
        if (requestList.getSelected() >= requestList.getChildCount()) {
            requestList.selected(-1);
        }
        cancelButton.enabled(requestList.getSelected() != -1);
        drawWindow(matrixStack);
    }
}
