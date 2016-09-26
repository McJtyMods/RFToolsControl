package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.events.TextSpecialKeyEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.gui.GuiTools;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.logic.editors.ParameterEditor;
import mcjty.rftoolscontrol.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterTypeTools;
import mcjty.rftoolscontrol.network.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiProcessor extends GenericGuiContainer<ProcessorTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/processor.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");

    private Window sideWindow;
    private EnergyBar energyBar;
    private ToggleButton[] setupButtons = new ToggleButton[ProcessorTileEntity.CARD_SLOTS];
    private WidgetList log;
    private WidgetList variableList;
    private TextField command;
    private ToggleButton exclusive;

    private static List<String> commandHistory = new ArrayList<>();
    private static int commandHistoryIndex = -1;

    private static List<String> fromServer_log = new ArrayList<>();
    public static void storeLogForClient(List<String> messages) {
        fromServer_log = new ArrayList<>(messages);
    }
    private static List<Parameter> fromServer_vars = new ArrayList<>();
    public static void storeVarsForClient(List<Parameter> messages) {
        fromServer_vars = new ArrayList<>(messages);
    }
    private int listDirty = 0;

    public GuiProcessor(ProcessorTileEntity tileEntity, ProcessorContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, RFToolsControl.GUI_MANUAL_CONTROL, "processor");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        // --- Main window ---
        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored)
                .setLayoutHint(new PositionalLayout.PositionalHint(122, 4, 70, 10))
                .setShowText(false).setHorizontal();
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        exclusive = new ToggleButton(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(122, 16, 70, 15))
                .setCheckMarker(true)
                .setText("Exclusive")
                .setTooltips(TextFormatting.YELLOW + "Exclusive mode", "If pressed then programs on", "card X can only run on core X");
        exclusive.setPressed(tileEntity.isExclusive());
        exclusive
                .addButtonEvent(parent -> {
                    tileEntity.setExclusive(exclusive.isPressed());
                    sendServerCommand(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_SETEXCLUSIVE, new Argument("v", exclusive.isPressed()));
                });
        toplevel.addChild(exclusive);

        setupLogWindow(toplevel);

        for (int i = 0; i < ProcessorTileEntity.CARD_SLOTS ; i++) {
            setupButtons[i] = new ToggleButton(mc, this)
                .addButtonEvent(this::setupMode)
                .setTooltips("Setup item and variable", "allocation for this card")
                .setLayoutHint(new PositionalLayout.PositionalHint(11 + i * 18, 6, 15, 7))
                .setUserObject("allowed");
            toplevel.addChild(setupButtons[i]);
        }
        window = new Window(this, toplevel);

        // --- Side window ---
        Panel listPanel = setupVariableListPanel();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground)
                .addChild(listPanel);
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);
    }

    private void setupLogWindow(Panel toplevel) {
        log = new WidgetList(mc, this).setFilledBackground(0xff000000).setFilledRectThickness(1)
                .setLayoutHint(new PositionalLayout.PositionalHint(9, 35, 173, 98))
                .setRowheight(14)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false);

        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(log)
                .setLayoutHint(new PositionalLayout.PositionalHint(183, 35, 9, 98));

        command = new TextField(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(9, 35+99, 183, 15))
                .addTextEnterEvent((e, text) -> executeCommand(text))
                .addSpecialKeyEvent(new TextSpecialKeyEvent() {
                    @Override
                    public void arrowUp(Widget widget) {
                        dumpHistory();
                        if (commandHistoryIndex == -1) {
                            commandHistoryIndex = commandHistory.size()-1;
                        } else {
                            commandHistoryIndex--;
                            if (commandHistoryIndex < 0) {
                                commandHistoryIndex = 0;
                            }
                        }
                        if (commandHistoryIndex >= 0 && commandHistoryIndex < commandHistory.size()) {
                            command.setText(commandHistory.get(commandHistoryIndex));
                        }
                        dumpHistory();
                    }

                    @Override
                    public void arrowDown(Widget widget) {
                        dumpHistory();
                        if (commandHistoryIndex != -1) {
                            commandHistoryIndex++;
                            if (commandHistoryIndex >= commandHistory.size()) {
                                commandHistoryIndex = -1;
                                command.setText("");
                            } else {
                                command.setText(commandHistory.get(commandHistoryIndex));
                            }
                        }
                        dumpHistory();
                    }

                    @Override
                    public void tab(Widget widget) {

                    }
                });

        toplevel.addChild(log).addChild(slider).addChild(command);
    }

    private void executeCommand(String text) {
        dumpHistory();
        sendServerCommand(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_CLEARLOG, new Argument("cmd", text));

        if (commandHistoryIndex >= 0 && commandHistoryIndex < commandHistory.size() && text.equals(commandHistory.get(commandHistoryIndex))) {
            // History command that didn't change
        } else if (!text.isEmpty()) {
            if (commandHistory.isEmpty() || !text.equals(commandHistory.get(commandHistory.size()-1))) {
                commandHistory.add(text);
            }
            while (commandHistory.size() > 50) {
                commandHistory.remove(0);
            }
            commandHistoryIndex = -1;
        }
        command.setText("");
        window.setTextFocus(command);
    }

    private void dumpHistory() {
//        System.out.println("##############");
//        int i = 0;
//        for (String s : commandHistory) {
//            if (i == commandHistoryIndex) {
//                System.out.println("* " + i + ": " + s + " *");
//            } else {
//                System.out.println("" + i + ": " + s);
//            }
//            i++;
//        }
    }

    private void requestLists() {
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetLog(tileEntity.getPos()));
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetVariables(tileEntity.getPos()));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestLists();
            listDirty = 10;
        }
    }

    private void populateLog() {
        boolean atend = log.getFirstSelected() + log.getCountSelected() >= log.getChildCount();
        log.removeChildren();
        for (String message : fromServer_log) {
            log.addChild(new Label(mc, this).setColor(0xff008800).setText(message).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
        }
        if (atend) {
            log.setFirstSelected(log.getChildCount());
        }
    }

    private void setupMode(Widget parent) {
        ToggleButton tb = (ToggleButton) parent;
        if (tb.isPressed()) {
            for (ToggleButton button : setupButtons) {
                if (button != tb) {
                    button.setPressed(false);
                }
            }
        }
        updateVariableList();
    }

    private int getSetupMode() {
        for (int i = 0 ; i < setupButtons.length ; i++) {
            if (setupButtons[i].isPressed()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            super.mouseClicked(x, y, button);
        } else {
            Optional<Widget> widget = getWindowManager().findWidgetAtPosition(x, y);
            if (widget.isPresent()) {
                Widget w = widget.get();
                if ("allowed".equals(w.getUserObject())) {
                    super.mouseClicked(x, y, button);
                    return;
                }
            }

            int leftx = window.getToplevel().getBounds().x;
            int topy = window.getToplevel().getBounds().y;
            x -= leftx;
            y -= topy;
            CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
            int itemAlloc = cardInfo.getItemAllocation();
            int varAlloc = cardInfo.getVarAllocation();

            for (int i = 0 ; i < ProcessorTileEntity.ITEM_SLOTS ; i++) {
                Slot slot = inventorySlots.getSlot(ProcessorContainer.SLOT_BUFFER + i);
                if (x >= slot.xDisplayPosition && x <= slot.xDisplayPosition + 17 && y >= slot.yDisplayPosition && y <= slot.yDisplayPosition + 17) {
                    boolean allocated = ((itemAlloc >> i) & 1) != 0;
                    allocated = !allocated;
                    if (allocated) {
                        itemAlloc = itemAlloc | (1 << i);
                    } else {
                        itemAlloc = itemAlloc & ~(1 << i);
                    }
                    cardInfo.setItemAllocation(itemAlloc);
                    sendServerCommand(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                            new Argument("card", setupMode),
                            new Argument("items", itemAlloc),
                            new Argument("vars", varAlloc));
                    break;
                }
            }
        }
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupVariableListPanel() {
        variableList = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 62, 220))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setUserObject("allowed");
        variableList.addSelectionEvent(new SelectionEvent() {
            @Override
            public void select(Widget parent, int i) {
                int setupMode = getSetupMode();
                if (setupMode != -1) {
                    CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
                    int varAlloc = cardInfo.getVarAllocation();
                    int itemAlloc = cardInfo.getItemAllocation();

                    boolean allocated = ((varAlloc >> i) & 1) != 0;
                    allocated = !allocated;
                    if (allocated) {
                        varAlloc = varAlloc | (1 << i);
                    } else {
                        varAlloc = varAlloc & ~(1 << i);
                    }
                    cardInfo.setVarAllocation(varAlloc);

                    Panel panel = (Panel) variableList.getChild(i);
                    panel.removeChildren();
                    int fill = allocated ? 0x7700ff00 : (tileEntity.isVarAllocated(-1, i) ? 0x77660000 : 0x77444444);
                    panel.setFilledBackground(fill);
                    panel.addChild(new Label(mc, GuiProcessor.this)
                            .setText(String.valueOf(i))
                            .setDesiredWidth(26)
                            .setUserObject("allowed"));
                    panel.addChild(new Button(mc, GuiProcessor.this)
                            .setText("...")
                            .setUserObject("allowed"));
                    panel.setUserObject("allowed");

                    sendServerCommand(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                            new Argument("card", setupMode),
                            new Argument("items", itemAlloc),
                            new Argument("vars", varAlloc));

                    variableList.setSelected(-1);
                }
            }

            @Override
            public void doubleClick(Widget parent, int index) {

            }
        });

        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(variableList)
                .setLayoutHint(new PositionalLayout.PositionalHint(62, 0, 9, 220))
                .setUserObject("allowed");

        updateVariableList();

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 72, 220))
                .addChild(variableList)
                .addChild(slider)
                .setUserObject("allowed");
//                .setFilledRectThickness(-2)
//                .setFilledBackground(StyleConfig.colorListBackground);
    }

    private void openValueEditor(int varIdx) {
        if (fromServer_vars == null || varIdx > fromServer_vars.size()) {
            return;
        }
        if (fromServer_vars.get(varIdx) == null) {
            GuiTools.showMessage(mc, this, getWindowManager(), 50, 50, "Variable is not defined!");
            return;
        }
        Parameter parameter = fromServer_vars.get(varIdx);
        if (parameter == null) {
            GuiTools.showMessage(mc, this, getWindowManager(), 50, 50, "Variable is not defined!");
            return;
        }
        ParameterType type = parameter.getParameterType();
        ParameterEditor editor = ParameterEditors.getEditor(type);
        Panel editPanel;
        if (editor != null) {
            editPanel = new Panel(mc, this).setLayout(new PositionalLayout())
                    .setFilledRectThickness(1);
            editor.build(mc, this, editPanel, o -> {
                NBTTagCompound tag = new NBTTagCompound();
                ParameterTypeTools.writeToNBT(tag, type, o);
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketVariableToServer(tileEntity.getPos(), varIdx, tag));
            });
            editor.writeValue(parameter.getParameterValue());
            editor.constantOnly();
        } else {
            return;
        }

        Panel panel = new Panel(mc, this)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        panel.setBounds(new Rectangle(50, 50, 200, 60 + editor.getHeight()));
        Window modalWindow = getWindowManager().createModalWindow(panel);
        panel.addChild(new Label(mc, this).setText("Var " + varIdx + ":"));
        panel.addChild(editPanel);
        panel.addChild(new Button(mc, this)
                .addButtonEvent(w ->  {
                    getWindowManager().closeWindow(modalWindow);
                })
                .setText("Close"));
    }

    private void updateVariableList() {
        variableList.removeChildren();
        int setupMode = getSetupMode();

        int varAlloc = 0;
        if (setupMode != -1) {
            CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
            varAlloc = cardInfo.getVarAllocation();
        }
        variableList.setPropagateEventsToChildren(setupMode == -1);

        for (int i = 0 ; i < tileEntity.getMaxvars() ; i++) {
            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredWidth(40);
            if (setupMode != -1) {
                boolean allocated = ((varAlloc >> i) & 1) != 0;
                int fill = allocated ? 0x7700ff00 : (tileEntity.isVarAllocated(-1, i) ? 0x77660000 : 0x77444444);
                panel.setFilledBackground(fill);
            }
            panel.addChild(new Label(mc, this).setText(String.valueOf(i)).setDesiredWidth(26).setUserObject("allowed"));
            int finalI = i;
            panel.addChild(new Button(mc, this)
                    .addButtonEvent(w -> openValueEditor(finalI))
                    .setText("...")
                    .setUserObject("allowed"));
            panel.setUserObject("allowed");
            variableList.addChild(panel);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        if (variableList.getChildCount() != tileEntity.getMaxvars()) {
            updateVariableList();
        }

        requestListsIfNeeded();
        populateLog();

        drawWindow();

        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsControl.MODID);

        drawAllocatedSlots();
    }

    private void drawAllocatedSlots() {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);

        CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
        int itemAlloc = cardInfo.getItemAllocation();

        for (int i = 0 ; i < ProcessorTileEntity.ITEM_SLOTS ; i++) {
            Slot slot = inventorySlots.getSlot(ProcessorContainer.SLOT_BUFFER + i);

            boolean allocated = ((itemAlloc >> i) & 1) != 0;
            int border = allocated ? 0xffffffff : 0xaaaaaaaa;
            int fill = allocated ? 0x7700ff00 : (tileEntity.isItemAllocated(-1, i) ? 0x77660000 : 0x77444444);
            RenderHelper.drawFlatBox(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 17, slot.yDisplayPosition + 17,
                    border, fill);
        }

        GlStateManager.popMatrix();
    }
}
