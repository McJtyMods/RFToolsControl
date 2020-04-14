package mcjty.rftoolscontrol.modules.processor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.events.TextSpecialKeyEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditor;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetFluids;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetLog;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetVariables;
import mcjty.rftoolscontrol.modules.processor.network.PacketVariableToServer;
import mcjty.rftoolscontrol.modules.processor.util.CardInfo;
import mcjty.rftoolscontrol.modules.programmer.client.GuiTools;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.TANKS;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;

public class GuiProcessor extends GenericGuiContainer<ProcessorTileEntity, ProcessorContainer> {
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
    private WidgetList fluidList;
    private TextField command;
    private ToggleButton exclusive;
    private ChoiceLabel hudMode;

    private static List<String> commandHistory = new ArrayList<>();
    private static int commandHistoryIndex = -1;

    private int[] fluidListMapping = new int[TANKS * 6];
    private static List<PacketGetFluids.FluidEntry> fromServer_fluids = new ArrayList<>();

    public static void storeFluidsForClient(List<PacketGetFluids.FluidEntry> messages) {
        fromServer_fluids = new ArrayList<>(messages);
    }

    private static List<Parameter> fromServer_vars = new ArrayList<>();

    public static void storeVarsForClient(List<Parameter> messages) {
        fromServer_vars = new ArrayList<>(messages);
    }

    private int listDirty = 0;

    public GuiProcessor(ProcessorTileEntity te, ProcessorContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/0, "processor");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        // --- Main window ---
        Panel toplevel = Widgets.positional().background(mainBackground);
        toplevel.bounds(guiLeft, guiTop, xSize, ySize);

        energyBar = new EnergyBar().vertical()
                .hint(122, 4, 70, 10)
                .showText(false).horizontal();
        toplevel.children(energyBar);

        exclusive = new ToggleButton()
                .hint(122, 16, 40, 15)
                .checkMarker(true)
                .text("Excl.")
                .tooltips(TextFormatting.YELLOW + "Exclusive mode", "If pressed then programs on", "card X can only run on core X");
        exclusive.pressed(tileEntity.isExclusive());
        exclusive
                .event(() -> {
                    tileEntity.setExclusive(exclusive.isPressed());
                    sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_SETEXCLUSIVE,
                            TypedMap.builder().put(PARAM_EXCLUSIVE, exclusive.isPressed()).build());
                });
        toplevel.children(exclusive);

        hudMode = new ChoiceLabel()
                .hint(122 + 40 + 1, 16, 28, 15)
                .choices("Off", "Log", "Db", "Gfx")
                .choiceTooltip("Off", "No overhead log")
                .choiceTooltip("Log", "Show the normal log")
                .choiceTooltip("Db", "Show a debug display")
                .choiceTooltip("Gfx", "Graphics display");
        switch (tileEntity.getShowHud()) {
            case HUD_OFF:
                hudMode.choice("Off");
                break;
            case HUD_LOG:
                hudMode.choice("Log");
                break;
            case HUD_DB:
                hudMode.choice("Db");
                break;
            case HUD_GFX:
                hudMode.choice("Gfx");
                break;
        }
        hudMode.event((newChoice) -> {
            String choice = hudMode.getCurrentChoice();
            int m = HUD_OFF;
            if ("Off".equals(choice)) {
                m = HUD_OFF;
            } else if ("Log".equals(choice)) {
                m = HUD_LOG;
            } else if ("Db".equals(choice)) {
                m = HUD_DB;
            } else {
                m = HUD_GFX;
            }
            sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_SETHUDMODE,
                    TypedMap.builder().put(PARAM_HUDMODE, m).build());
        });
        toplevel.children(hudMode);

        setupLogWindow(toplevel);

        for (int i = 0; i < ProcessorTileEntity.CARD_SLOTS; i++) {
            int finalI = i;
            setupButtons[i] = new ToggleButton()
                    .event(() -> setupMode(setupButtons[finalI]))
                    .tooltips(TextFormatting.YELLOW + "Resource allocation", "Setup item and variable", "allocation for this card")
                    .hint(11 + i * 18, 6, 15, 7)
                    .userObject("allowed");
            toplevel.children(setupButtons[i]);
        }
        window = new Window(this, toplevel);

        // --- Side window ---
        Panel listPanel = setupVariableListPanel();
        Panel sidePanel = Widgets.positional().background(sideBackground)
                .children(listPanel);
        sidePanel.bounds(guiLeft - SIDEWIDTH, guiTop, SIDEWIDTH, ySize);
        sideWindow = new Window(this, sidePanel);

        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    private void setupLogWindow(Panel toplevel) {
        log = list(9, 35, 173, 98).name("log").filledBackground(0xff000000).filledRectThickness(1)
                .rowheight(14)
                .invisibleSelection(true)
                .drawHorizontalLines(false);

        Slider slider = slider(183, 35, 9, 98)
                .vertical()
                .scrollableName("log");

        command = textfield(9, 35 + 99, 183, 15)
                .addTextEnterEvent(this::executeCommand)
                .specialKeyEvent(new TextSpecialKeyEvent() {
                    @Override
                    public void arrowUp() {
                        dumpHistory();
                        if (commandHistoryIndex == -1) {
                            commandHistoryIndex = commandHistory.size() - 1;
                        } else {
                            commandHistoryIndex--;
                            if (commandHistoryIndex < 0) {
                                commandHistoryIndex = 0;
                            }
                        }
                        if (commandHistoryIndex >= 0 && commandHistoryIndex < commandHistory.size()) {
                            command.text(commandHistory.get(commandHistoryIndex));
                        }
                        dumpHistory();
                    }

                    @Override
                    public void arrowDown() {
                        dumpHistory();
                        if (commandHistoryIndex != -1) {
                            commandHistoryIndex++;
                            if (commandHistoryIndex >= commandHistory.size()) {
                                commandHistoryIndex = -1;
                                command.text("");
                            } else {
                                command.text(commandHistory.get(commandHistoryIndex));
                            }
                        }
                        dumpHistory();
                    }

                    @Override
                    public void tab() {

                    }
                });

        toplevel.children(log, slider, command);
    }

    private void executeCommand(String text) {
        dumpHistory();
        sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_EXECUTE,
                TypedMap.builder().put(PARAM_CMD, text).build());

        if (commandHistoryIndex >= 0 && commandHistoryIndex < commandHistory.size() && text.equals(commandHistory.get(commandHistoryIndex))) {
            // History command that didn't change
        } else if (!text.isEmpty()) {
            if (commandHistory.isEmpty() || !text.equals(commandHistory.get(commandHistory.size() - 1))) {
                commandHistory.add(text);
            }
            while (commandHistory.size() > 50) {
                commandHistory.remove(0);
            }
            commandHistoryIndex = -1;
        }
        command.text("");
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
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetFluids(tileEntity.getPos()));
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
        for (String message : tileEntity.getClientLog()) {
            log.children(label(message).color(0xff008800).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        }
        if (atend) {
            log.setFirstSelected(log.getChildCount());
        }
    }

    private void setupMode(Widget<?> parent) {
        ToggleButton tb = (ToggleButton) parent;
        if (tb.isPressed()) {
            for (ToggleButton button : setupButtons) {
                if (button != tb) {
                    button.pressed(false);
                }
            }
        }
        updateVariableList();
    }

    private int getSetupMode() {
        for (int i = 0; i < setupButtons.length; i++) {
            if (setupButtons[i].isPressed()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            super.mouseClicked(x, y, button);
        } else {
            Optional<Widget<?>> widget = getWindowManager().findWidgetAtPosition(x, y);
            if (widget.isPresent()) {
                Widget<?> w = widget.get();
                if ("allowed".equals(w.getUserObject())) {
                    return super.mouseClicked(x, y, button);
                }
            }

            int leftx = window.getToplevel().getBounds().x;
            int topy = window.getToplevel().getBounds().y;
            x -= leftx;
            y -= topy;
            CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
            int itemAlloc = cardInfo.getItemAllocation();
            int varAlloc = cardInfo.getVarAllocation();
            int fluidAlloc = cardInfo.getFluidAllocation();

            for (int i = 0; i < ProcessorTileEntity.ITEM_SLOTS; i++) {
                Slot slot = container.getSlot(ProcessorContainer.SLOT_BUFFER + i);
                if (x >= slot.xPos && x <= slot.xPos + 17
                        && y >= slot.yPos && y <= slot.yPos + 17) {
                    boolean allocated = ((itemAlloc >> i) & 1) != 0;
                    allocated = !allocated;
                    if (allocated) {
                        itemAlloc = itemAlloc | (1 << i);
                    } else {
                        itemAlloc = itemAlloc & ~(1 << i);
                    }
                    cardInfo.setItemAllocation(itemAlloc);
                    sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                            TypedMap.builder()
                                    .put(PARAM_CARD, setupMode)
                                    .put(PARAM_ITEMS, itemAlloc)
                                    .put(PARAM_VARS, varAlloc)
                                    .put(PARAM_FLUID, fluidAlloc)
                                    .build());
                    break;
                }
            }
        }
        return true;    // @todo 1.15 right return?
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupVariableListPanel() {
        fluidList = list(0, 0, 62, 65)
                .name("fluids")
                .propagateEventsToChildren(true)
                .invisibleSelection(true)
                .drawHorizontalLines(false)
                .userObject("allowed");
        fluidList.event(new SelectionEvent() {
            @Override
            public void select(int i) {
                int setupMode = getSetupMode();
                if (setupMode != -1) {
                    CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
                    int varAlloc = cardInfo.getVarAllocation();
                    int itemAlloc = cardInfo.getItemAllocation();
                    int fluidAlloc = cardInfo.getFluidAllocation();

                    int idx = fluidListMapping[i];

                    boolean allocated = ((fluidAlloc >> idx) & 1) != 0;
                    allocated = !allocated;
                    if (allocated) {
                        fluidAlloc = fluidAlloc | (1 << idx);
                    } else {
                        fluidAlloc = fluidAlloc & ~(1 << idx);
                    }
                    cardInfo.setFluidAllocation(fluidAlloc);

                    sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                            TypedMap.builder()
                                    .put(PARAM_CARD, setupMode)
                                    .put(PARAM_ITEMS, itemAlloc)
                                    .put(PARAM_VARS, varAlloc)
                                    .put(PARAM_FLUID, fluidAlloc)
                                    .build());

                    updateFluidList();
                    fluidList.selected(-1);
                }
            }

            @Override
            public void doubleClick(int index) {

            }
        });

        Slider fluidSlider = slider(62, 0, 9, 65)
                .vertical()
                .scrollableName("fluids")
                .userObject("allowed");

        updateFluidList();

        variableList = list(0, 67, 62, 161)
                .name("variables")
                .propagateEventsToChildren(true)
                .invisibleSelection(true)
                .drawHorizontalLines(false)
                .userObject("allowed");
        variableList.event(new SelectionEvent() {
            @Override
            public void select(int i) {
                int setupMode = getSetupMode();
                if (setupMode != -1) {
                    CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
                    int varAlloc = cardInfo.getVarAllocation();
                    int itemAlloc = cardInfo.getItemAllocation();
                    int fluidAlloc = cardInfo.getFluidAllocation();

                    boolean allocated = ((varAlloc >> i) & 1) != 0;
                    allocated = !allocated;
                    if (allocated) {
                        varAlloc = varAlloc | (1 << i);
                    } else {
                        varAlloc = varAlloc & ~(1 << i);
                    }
                    cardInfo.setVarAllocation(varAlloc);

                    sendServerCommandTyped(RFToolsCtrlMessages.INSTANCE, ProcessorTileEntity.CMD_ALLOCATE,
                            TypedMap.builder()
                                    .put(PARAM_CARD, setupMode)
                                    .put(PARAM_ITEMS, itemAlloc)
                                    .put(PARAM_VARS, varAlloc)
                                    .put(PARAM_FLUID, fluidAlloc)
                                    .build());

                    updateVariableList();

                    variableList.selected(-1);
                }
            }

            @Override
            public void doubleClick(int index) {

            }
        });

        Slider varSlider = slider(62, 67, 9, 161)
                .vertical()
                .scrollableName("variables")
                .userObject("allowed");

        updateVariableList();

        return Widgets.positional().hint(5, 5, 72, 220)
                .children(variableList, varSlider, fluidList, fluidSlider)
                .userObject("allowed");
    }

    private void openValueEditor(int varIdx) {
        if (fromServer_vars == null || varIdx > fromServer_vars.size()) {
            return;
        }
        if (fromServer_vars.get(varIdx) == null) {
            GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, "Variable is not defined!");
            return;
        }
        Parameter parameter = fromServer_vars.get(varIdx);
        if (parameter == null) {
            GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, "Variable is not defined!");
            return;
        }
        ParameterType type = parameter.getParameterType();
        ParameterEditor editor = ParameterEditors.getEditor(type);
        Panel editPanel;
        if (editor != null) {
            editPanel = Widgets.positional()
                    .filledRectThickness(1);
            editor.build(minecraft, this, editPanel, o -> {
                CompoundNBT tag = new CompoundNBT();
                ParameterTypeTools.writeToNBT(tag, type, o);
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketVariableToServer(tileEntity.getPos(), varIdx, tag));
            });
            editor.writeValue(parameter.getParameterValue());
            editor.constantOnly();
        } else {
            return;
        }

        Panel panel = vertical()
                .filledBackground(0xff666666, 0xffaaaaaa)
                .filledRectThickness(1);
        panel.bounds(50, 50, 200, 60 + editor.getHeight());
        Window modalWindow = getWindowManager().createModalWindow(panel);
        panel.children(label("Var " + varIdx + ":"),
                editPanel, button("Close").channel("close"));

        modalWindow.event("close", (source, params) -> getWindowManager().closeWindow(modalWindow));
    }

    private void updateFluidList() {
        fluidList.removeChildren();
        for (int i = 0; i < fluidListMapping.length; i++) {
            fluidListMapping[i] = -1;
        }

        int setupMode = getSetupMode();

        int fluidAlloc = 0;
        if (setupMode != -1) {
            CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
            fluidAlloc = cardInfo.getFluidAllocation();
        }
        fluidList.propagateEventsToChildren(setupMode == -1);

        int index = 0;
        for (int i = 0; i < fromServer_fluids.size(); i++) {
            PacketGetFluids.FluidEntry entry = fromServer_fluids.get(i);
            if (entry.isAllocated()) {
                fluidListMapping[fluidList.getChildCount()] = i;
                Direction side = Direction.values()[i / TANKS];
                String l = side.getName().substring(0, 1).toUpperCase() + (i % TANKS);
                Panel panel = horizontal().desiredWidth(40);
                AbstractWidget<?> label;
                if (setupMode != -1) {
                    boolean allocated = ((fluidAlloc >> i) & 1) != 0;
                    int fill = allocated ? 0x7700ff00 : (tileEntity.isFluidAllocated(-1, i) ? 0x77660000 : 0x77444444);
                    panel.filledBackground(fill);
                    if (allocated) {
                        label = label(String.valueOf(index)).color(0xffffffff).desiredWidth(26).userObject("allowed");
                        index++;
                    } else {
                        label = label("/").desiredWidth(26).userObject("allowed");
                    }
                } else {
                    label = label(l).desiredWidth(26).userObject("allowed");
                }
                label.userObject("allowed");
                panel.children(label);
                FluidStack fluidStack = entry.getFluidStack();
                if (fluidStack != null) {
                    BlockRender fluid = new BlockRender().renderItem(fluidStack);
                    fluid.tooltips(
                            TextFormatting.GREEN + "Fluid: " + TextFormatting.WHITE + fluidStack.getDisplayName().getFormattedText(),
                            TextFormatting.GREEN + "Amount: " + TextFormatting.WHITE + fluidStack.getAmount() + "mb");
                    fluid.userObject("allowed");
                    panel.children(fluid);
                }
                panel.userObject("allowed");
                fluidList.children(panel);
            }
        }
    }

    private void updateVariableList() {
        variableList.removeChildren();
        int setupMode = getSetupMode();

        int varAlloc = 0;
        if (setupMode != -1) {
            CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
            varAlloc = cardInfo.getVarAllocation();
        }
        variableList.propagateEventsToChildren(setupMode == -1);

        int index = 0;
        for (int i = 0; i < tileEntity.getMaxvars(); i++) {
            Panel panel = horizontal().desiredWidth(40);
            if (setupMode != -1) {
                boolean allocated = ((varAlloc >> i) & 1) != 0;
                int fill = allocated ? 0x7700ff00 : (tileEntity.isVarAllocated(-1, i) ? 0x77660000 : 0x77444444);
                panel.filledBackground(fill);
                if (allocated) {
                    panel.children(label(String.valueOf(index)).color(0xffffffff).desiredWidth(26).userObject("allowed"));
                    index++;
                } else {
                    panel.children(label("/").desiredWidth(26).userObject("allowed"));
                }
            } else {
                panel.children(label(String.valueOf(i)).desiredWidth(26).userObject("allowed"));
            }
            int finalI = i;
            panel.children(button("...")
                    .event(() -> openValueEditor(finalI))
                    .userObject("allowed"));
            panel.userObject("allowed");
            variableList.children(panel);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        if (variableList.getChildCount() != tileEntity.getMaxvars()) {
            updateVariableList();
        }
        updateFluidList();

        requestListsIfNeeded();
        populateLog();

        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.maxValue(((GenericEnergyStorage) e).getCapacity());
            energyBar.value(((GenericEnergyStorage) e).getEnergy());
        });

        drawAllocatedSlots();
    }

    private void drawAllocatedSlots() {
        int setupMode = getSetupMode();
        if (setupMode == -1) {
            return;
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef(guiLeft, guiTop, 0.0F);

        CardInfo cardInfo = tileEntity.getCardInfo(setupMode);
        int itemAlloc = cardInfo.getItemAllocation();

        int index = 0;
        for (int i = 0; i < ProcessorTileEntity.ITEM_SLOTS; i++) {
            Slot slot = container.getSlot(ProcessorContainer.SLOT_BUFFER + i);

            boolean allocated = ((itemAlloc >> i) & 1) != 0;
            int border = allocated ? 0xffffffff : 0xaaaaaaaa;
            int fill = allocated ? 0x7700ff00 : (tileEntity.isItemAllocated(-1, i) ? 0x77660000 : 0x77444444);
            RenderHelper.drawFlatBox(slot.xPos, slot.yPos,
                    slot.xPos + 17, slot.yPos + 17,
                    border, fill);
            if (allocated) {
                this.drawString(minecraft.fontRenderer, "" + index,
                        slot.xPos + 4, slot.yPos + 4, 0xffffffff);
                index++;
            }
        }

        RenderSystem.popMatrix();
    }
}
