package mcjty.rftoolscontrol.modules.programmer.client;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.icons.IIcon;
import mcjty.lib.gui.icons.ImageIcon;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.control.code.Opcode;
import mcjty.rftoolsbase.api.control.code.OpcodeCategory;
import mcjty.rftoolsbase.api.control.code.OpcodeOutput;
import mcjty.rftoolsbase.api.control.parameters.ParameterDescription;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.logic.Connection;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.ProgramValidator;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditor;
import mcjty.rftoolscontrol.modules.processor.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.modules.processor.logic.grid.GridInstance;
import mcjty.rftoolscontrol.modules.processor.logic.grid.GridPos;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerContainer;
import mcjty.rftoolscontrol.modules.programmer.blocks.ProgrammerTileEntity;
import mcjty.rftoolscontrol.modules.programmer.network.PacketUpdateNBTItemInventoryProgrammer;
import mcjty.rftoolscontrol.modules.various.items.ProgramCardItem;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;
import java.util.*;

import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiProgrammer extends GenericGuiContainer<ProgrammerTileEntity, ProgrammerContainer> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    public static final int GRID_HEIGHT = 10;
    public static final int GRID_WIDTH = 11;

    public static int ICONSIZE = 20;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/programmer.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsControl.MODID, "textures/gui/guielements.png");

    private Window sideWindow;
    private WidgetList gridList;
    private WidgetList editorList;
    private WidgetList opcodeList;
    private IconHolder trashcan;
    private List<ImageChoiceLabel> categoryLabels = new ArrayList<>();

    private int iconLeavesFromX = -1;
    private int iconLeavesFromY = -1;
    private boolean loading = false;
    private OpcodeCategory currentCategory = null;

    private static final Map<String, IIcon> ICONS = new HashMap<>();
    private static final Map<Connection, IIcon> CONNECTION_ICONS = new HashMap<>();
    private static final Map<Connection, IIcon> HIGHLIGHT_ICONS = new HashMap<>();
    private static final IIcon selectionIcon;
    private static final IIcon errorIcon1;
    private static final IIcon errorIcon2;

    private static ProgramCardInstance undoProgram = null;

    private GridPos prevHighlightConnector = null;

    static {
        CONNECTION_ICONS.put(Connection.UP, new ImageIcon(Connection.UP.getId()).setImage(icons, 0 * ICONSIZE, 5 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.UP_NEG, new ImageIcon(Connection.UP_NEG.getId()).setImage(icons, 0 * ICONSIZE, 6 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.RIGHT, new ImageIcon(Connection.RIGHT.getId()).setImage(icons, 1 * ICONSIZE, 5 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.RIGHT_NEG, new ImageIcon(Connection.RIGHT_NEG.getId()).setImage(icons, 1 * ICONSIZE, 6 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.DOWN, new ImageIcon(Connection.DOWN.getId()).setImage(icons, 2 * ICONSIZE, 5 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.DOWN_NEG, new ImageIcon(Connection.DOWN_NEG.getId()).setImage(icons, 2 * ICONSIZE, 6 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.LEFT, new ImageIcon(Connection.LEFT.getId()).setImage(icons, 3 * ICONSIZE, 5 * ICONSIZE));
        CONNECTION_ICONS.put(Connection.LEFT_NEG, new ImageIcon(Connection.LEFT_NEG.getId()).setImage(icons, 3 * ICONSIZE, 6 * ICONSIZE));

        HIGHLIGHT_ICONS.put(Connection.UP, new ImageIcon("H").setImage(icons, 0 * ICONSIZE, 7 * ICONSIZE));
        HIGHLIGHT_ICONS.put(Connection.RIGHT, new ImageIcon("H").setImage(icons, 1 * ICONSIZE, 7 * ICONSIZE));
        HIGHLIGHT_ICONS.put(Connection.DOWN, new ImageIcon("H").setImage(icons, 2 * ICONSIZE, 7 * ICONSIZE));
        HIGHLIGHT_ICONS.put(Connection.LEFT, new ImageIcon("H").setImage(icons, 3 * ICONSIZE, 7 * ICONSIZE));

        for (IIcon icon : CONNECTION_ICONS.values()) {
            ((ImageIcon) icon).setDimensions(ICONSIZE, ICONSIZE);
        }
        for (IIcon icon : HIGHLIGHT_ICONS.values()) {
            ((ImageIcon) icon).setDimensions(ICONSIZE, ICONSIZE);
        }

        selectionIcon = new ImageIcon("S").setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 0 * ICONSIZE, 8 * ICONSIZE);
        errorIcon1 = new ImageIcon("E1").setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 1 * ICONSIZE, 8 * ICONSIZE);
        errorIcon2 = new ImageIcon("E2").setDimensions(ICONSIZE, ICONSIZE).setImage(icons, 2 * ICONSIZE, 8 * ICONSIZE);
    }

    public GuiProgrammer(ProgrammerTileEntity te, ProgrammerContainer container, PlayerInventory inventory) {
        super(RFToolsControl.instance, te, container, inventory, ManualHelper.create("rftoolscontrol:programmer/programmer_intro"));

        xSize = WIDTH;
        ySize = HEIGHT;

        initIcons();
    }

    private void initIcons() {
        if (ICONS.isEmpty()) {
            for (Map.Entry<String, Opcode> entry : Opcodes.OPCODES.entrySet()) {
                String id = entry.getKey();
                Opcode opcode = entry.getValue();
                ResourceLocation iconResource = icons;
                if (opcode.getIconResource() != null) {
                    iconResource = new ResourceLocation(opcode.getIconResource());
                }
                ICONS.put(id, new ImageIcon(id).setDimensions(ICONSIZE, ICONSIZE).setImage(iconResource, opcode.getIconU() * ICONSIZE, opcode.getIconV() * ICONSIZE));
            }
        }
    }

    @Override
    public void init() {
        super.init();

        // --- Main window ---
        Panel editorPanel = setupEditorPanel();
        Panel controlPanel = setupControlPanel();
        Panel gridPanel = setupGridPanel();
        Panel toplevel = positional().background(mainBackground)
                .children(editorPanel, controlPanel, gridPanel);
        toplevel.bounds(guiLeft, guiTop, xSize, ySize);
        window = new Window(this, toplevel).addFocusEvent((focused) -> selectIcon(window, focused));

        // --- Side window ---
        Panel listPanel = setupListPanel();
        Panel sidePanel = positional().background(sideBackground).children(listPanel);
        sidePanel.bounds(guiLeft - SIDEWIDTH, guiTop, SIDEWIDTH, ySize);
        sideWindow = new Window(this, sidePanel);

        loadProgram(ProgrammerContainer.SLOT_DUMMY);

        clearCategoryLabels();
        minecraft.keyboardListener.enableRepeatEvents(true);
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private long prevTime = -1L;

    private Panel setupGridPanel() {

        Panel panel = positional().hint(5, 5, 246, 130);

        gridList = list(0, 0, 236, 130)
                .name("grid")
                .propagateEventsToChildren(true)
                .invisibleSelection(true)
                .drawHorizontalLines(false)
                .rowheight(ICONSIZE + 1);
        Slider slider = slider(237, 0, 9, 130)
                .vertical()
                .scrollableName("grid");

        for (int y = 0; y < GRID_HEIGHT; y++) {
            Panel rowPanel = new Panel().layout(new HorizontalLayout().setSpacing(-1).setHorizontalMargin(0).setVerticalMargin(0));
            for (int x = 0; x < GRID_WIDTH; x++) {
                int finalX = x;
                int finalY = y;
                IconHolder holder = new IconHolder() {
                    @Override
                    public List<String> getTooltips() {
                        return getGridIconTooltips(finalX, finalY);
                    }
                }
                        .desiredWidth(ICONSIZE + 2)
                        .desiredHeight(ICONSIZE + 2)
                        .border(1)
                        .borderColor(0xff777777)
                        .selectable(true)
                        .userObject(new GridPos(finalX, finalY))
                        .hoverEvent(((iIcon, dx, dy) -> {
                            handleConnectorHighlight(finalX, finalY, iIcon, dx, dy);
                        }))
                        .leavesEvent(((icon) -> {
                            iconLeavesFromX = finalX;
                            iconLeavesFromY = finalY;
                            return true;
                        }))
                        .arrivesEvent(((icon) -> {
                            if (icon != null && !loading) {
                                handleNewIconOverlay(icon, finalX, finalY);
                            }
                            return true;
                        }))
                        .clickedEvent((icon, dx, dy) -> {
                            gridIconClicked(icon, finalX, finalY, dx, dy);
                            return true;
                        });
                rowPanel.children(holder);
            }
            gridList.children(rowPanel);
        }

        panel.children(gridList).children(slider);

        return panel;
    }

    private List<String> getGridIconTooltips(int finalX, int finalY) {
        if (McJtyLib.proxy.isCtrlKeyDown()) {
            List<String> tooltips = new ArrayList<>();
            if (Config.tooltipVerbosityLevel.get() >= 2) {
                tooltips.add(TextFormatting.GREEN + "Ctrl-Click to add or remove selection");
                tooltips.add(TextFormatting.GREEN + "Ctrl-Double click to (de)select sequence");
                tooltips.add(TextFormatting.YELLOW + "Ctrl-A to select entire grid");
                tooltips.add(TextFormatting.YELLOW + "Ctrl-C to copy selected grid");
                tooltips.add(TextFormatting.YELLOW + "Ctrl-X to cut selected grid");
                tooltips.add(TextFormatting.YELLOW + "Ctrl-V to paste to selected grid");
                tooltips.add(TextFormatting.YELLOW + "Ctrl-Z undo last paste operation");
            } else if (Config.tooltipVerbosityLevel.get() >= 1) {
                tooltips.add(TextFormatting.GREEN + "Use Ctrl with A, C, X, V or Z");
            }
            return tooltips;
        } else if (prevHighlightConnector != null) {
            List<String> tooltips = new ArrayList<>();
            if (Config.doubleClickToChangeConnector.get()) {
                tooltips.add(TextFormatting.GREEN + "Double click to change connector");
            } else {
                tooltips.add(TextFormatting.GREEN + "Click to change connector");
            }
            return tooltips;
        } else {
            return getIconTooltipGrid(finalX, finalY);
        }
    }

    private void selectSequence(GridPos pos, Set<GridPos> done, boolean select) {
        if (!checkValidGridPos(pos)) {
            return;
        }
        if (done.contains(pos)) {
            return;
        }
        IIcon icon = getHolder(pos.getX(), pos.getY()).getIcon();
        if (icon == null) {
            return;
        }
        icon.removeOverlay("S");
        if (select) {
            icon.addOverlay(selectionIcon);
        }
        done.add(pos);
        selectSequence(pos.up(), done, select);
        selectSequence(pos.down(), done, select);
        selectSequence(pos.left(), done, select);
        selectSequence(pos.right(), done, select);
    }

    private boolean checkValidGridPos(GridPos pos) {
        if (pos.getX() < 0 || pos.getX() >= GRID_WIDTH) {
            return false;
        }
        if (pos.getY() < 0 || pos.getY() >= GRID_HEIGHT) {
            return false;
        }
        return true;
    }

    private void gridIconClicked(IIcon icon, int x, int y, int dx, int dy) {
        if (McJtyLib.proxy.isCtrlKeyDown()) {
            long time = System.currentTimeMillis();
            boolean doubleclick = false;
            if (prevTime != -1L && time - prevTime < 250L) {
                doubleclick = true;
            }
            prevTime = time;

            if (icon.hasOverlay("S")) {
                if (doubleclick) {
                    selectSequence(new GridPos(x, y), new HashSet<>(), true);  // Reverse because first click also did something
                } else {
                    icon.removeOverlay("S");
                }
            } else {
                if (doubleclick) {
                    selectSequence(new GridPos(x, y), new HashSet<>(), false);  // Reverse because first click also did something
                } else {
                    icon.addOverlay(selectionIcon);
                }
            }
            return;
        }

        clearSelection();

        long time = System.currentTimeMillis();
        boolean doubleclick = !Config.doubleClickToChangeConnector.get();
        if (prevTime != -1L && time - prevTime < 250L) {
            doubleclick = true;
        }
        prevTime = time;
        if (doubleclick) {
            Connection connection = getConnectionHandle(dx, dy);
            if (connection != null) {
                handleIconOverlay(icon, connection);
            }
        }
    }

    private void selectAll() {
        for (int ix = 0; ix < GRID_WIDTH; ix++) {
            for (int iy = 0; iy < GRID_HEIGHT; iy++) {
                IIcon i = getHolder(ix, iy).getIcon();
                if (i != null) {
                    i.addOverlay(selectionIcon);
                }
            }
        }
    }

    private void clearSelection() {
        for (int ix = 0; ix < GRID_WIDTH; ix++) {
            for (int iy = 0; iy < GRID_HEIGHT; iy++) {
                IIcon i = getHolder(ix, iy).getIcon();
                if (i != null) {
                    i.removeOverlay("S");
                }
            }
        }
    }

    private boolean checkSelection() {
        for (int ix = 0; ix < GRID_WIDTH; ix++) {
            for (int iy = 0; iy < GRID_HEIGHT; iy++) {
                IIcon i = getHolder(ix, iy).getIcon();
                if (i != null) {
                    if (i.hasOverlay("S")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void handleConnectorHighlight(int finalX, int finalY, IIcon iIcon, int dx, int dy) {
        if (prevHighlightConnector != null) {
            IconHolder h = getHolder(prevHighlightConnector.getX(), prevHighlightConnector.getY());
            if (h.getIcon() != null) {
                h.getIcon().removeOverlay("H");
            }
            prevHighlightConnector = null;
        }

        if (iIcon == null) {
            return;
        }

        iIcon.removeOverlay("H");
        Connection connection = getConnectionHandle(dx, dy);
        if (connection != null) {
            iIcon.addOverlay(HIGHLIGHT_ICONS.get(connection));
            prevHighlightConnector = new GridPos(finalX, finalY);
        }
    }

    private Connection getConnectionHandle(int dx, int dy) {
        if (dy <= 5 && dx >= 8 && dx <= 15) {
            return Connection.UP;
        } else if (dy >= ICONSIZE - 3 && dx >= 8 && dx <= 15) {
            return Connection.DOWN;
        } else if (dx <= 5 && dy >= 7 && dy <= 14) {
            return Connection.LEFT;
        } else if (dx >= ICONSIZE - 3 && dy >= 7 && dy <= 14) {
            return Connection.RIGHT;
        }
        return null;
    }

    // Try to make a connection to this one in case there are no connections yet
    private void handleNewIconOverlay(IIcon icon, int x, int y) {
        // We didn't move, do nothing
        if (x == iconLeavesFromX && y == iconLeavesFromY) {
            return;
        }
        Opcode opcode = Opcodes.OPCODES.get(icon.getID());
        if (opcode.isEvent()) {
            return;
        }
        tryConnectToThis(x - 1, y, icon, Connection.RIGHT);
        tryConnectToThis(x + 1, y, icon, Connection.LEFT);
        tryConnectToThis(x, y - 1, icon, Connection.DOWN);
        tryConnectToThis(x, y + 1, icon, Connection.UP);
    }

    private void tryConnectToThis(int x, int y, IIcon icon, Connection connection) {
        if (x < 0 || x >= GRID_WIDTH) {
            return;
        }
        if (y < 0 || y >= GRID_HEIGHT) {
            return;
        }
        IconHolder holder = getHolder(x, y);
        IIcon sourceIcon = holder.getIcon();
        if (sourceIcon != null) {
            Opcode opcode = Opcodes.OPCODES.get(sourceIcon.getID());
            if (opcode.getOpcodeOutput() == OpcodeOutput.NONE) {
                return;
            } else if (opcode.getOpcodeOutput() == OpcodeOutput.SINGLE) {
                int cnt = countConnections(sourceIcon);
                if (cnt == 0) {
                    sourceIcon.addOverlay(CONNECTION_ICONS.get(connection));
                }
            } else if (opcode.getOpcodeOutput() == OpcodeOutput.YESNO) {
                int cnt = countPrimaryConnections(sourceIcon);
                if (cnt == 0) {
                    sourceIcon.addOverlay(CONNECTION_ICONS.get(connection));
                } else {
                    cnt = countSecondaryConnections(sourceIcon);
                    if (cnt == 0) {
                        sourceIcon.addOverlay(CONNECTION_ICONS.get(connection.getOpposite()));
                    }
                }
            }
        }
    }

    private int countConnections(IIcon sourceIcon) {
        int cnt = 0;
        for (Connection connection : Connection.values()) {
            if (sourceIcon.hasOverlay(connection.getId())) {
                cnt++;
            }
        }
        return cnt;
    }

    private int countPrimaryConnections(IIcon sourceIcon) {
        int cnt = 0;
        for (Connection connection : Connection.values()) {
            if (connection.isPrimary()) {
                if (sourceIcon.hasOverlay(connection.getId())) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    private int countSecondaryConnections(IIcon sourceIcon) {
        int cnt = 0;
        for (Connection connection : Connection.values()) {
            if (!connection.isPrimary()) {
                if (sourceIcon.hasOverlay(connection.getId())) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    private void handleIconOverlay(IIcon icon, Connection connection) {
        Opcode opcode = Opcodes.OPCODES.get(icon.getID());
        if (opcode.getOpcodeOutput() == OpcodeOutput.NONE) {
            return;
        }
        if (opcode.getOpcodeOutput() == OpcodeOutput.SINGLE) {
            boolean has = icon.hasOverlay(connection.getId());
            for (Connection c : Connection.values()) {
                icon.removeOverlay(c.getId());
            }
            if (!has) {
                if (!icon.hasOverlay(connection.getId())) {
                    icon.addOverlay(CONNECTION_ICONS.get(connection));
                }
            }
        } else {
            if (icon.hasOverlay(connection.getId())) {
                icon.removeOverlay(Connection.DOWN_NEG.getId());
                icon.removeOverlay(Connection.UP_NEG.getId());
                icon.removeOverlay(Connection.LEFT_NEG.getId());
                icon.removeOverlay(Connection.RIGHT_NEG.getId());
                icon.removeOverlay(connection.getId());
                icon.addOverlay(CONNECTION_ICONS.get(connection.getOpposite()));
            } else if (icon.hasOverlay(connection.getOpposite().getId())) {
                icon.removeOverlay(connection.getOpposite().getId());
            } else {
                if (connection.isPrimary()) {
                    icon.removeOverlay(Connection.DOWN.getId());
                    icon.removeOverlay(Connection.UP.getId());
                    icon.removeOverlay(Connection.LEFT.getId());
                    icon.removeOverlay(Connection.RIGHT.getId());
                } else {
                    icon.removeOverlay(Connection.DOWN_NEG.getId());
                    icon.removeOverlay(Connection.UP_NEG.getId());
                    icon.removeOverlay(Connection.LEFT_NEG.getId());
                    icon.removeOverlay(Connection.RIGHT_NEG.getId());
                }
                icon.addOverlay(CONNECTION_ICONS.get(connection));
            }
        }
    }

    private void clearGrid(boolean selection) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                IconHolder h = getHolder(x, y);
                if ((!selection) || (h.getIcon() != null && h.getIcon().hasOverlay("S"))) {
                    h.setIcon(null);
                }
            }
        }
    }

    private IconHolder getHolder(int x, int y) {
        Panel row = (Panel) gridList.getChild(y);
        return (IconHolder) row.getChild(x);
    }

    private void validateAndHilight() {
        ProgramCardInstance instance = makeGridInstance(false);

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                IconHolder h = getHolder(x, y);
                if (h.getIcon() != null) {
                    h.getIcon().removeOverlay("E1");
                    h.getIcon().removeOverlay("E2");
                }
            }
        }

        long time = System.currentTimeMillis();
        List<Pair<GridPos, String>> errors = ProgramValidator.validate(instance);
        for (Pair<GridPos, String> entry : errors) {
            GridPos p = entry.getKey();
            IconHolder h = getHolder(p.getX(), p.getY());
            h.getIcon().addOverlay((time % 2000) < 1000 ? errorIcon1 : errorIcon2);
        }
    }

    private void validateProgram() {
        Panel panel = vertical()
                .filledBackground(0xff666666, 0xffaaaaaa)
                .filledRectThickness(1);
        panel.bounds(60, 10, 200, 130);
        Window modalWindow = getWindowManager().createModalWindow(panel);
        WidgetList errorList = new WidgetList();
        errorList.event(new SelectionEvent() {
            @Override
            public void select(int index) {
            }

            @Override
            public void doubleClick(int index) {
                if (errorList.getSelected() != -1) {
                    Widget<?> child = errorList.getChild(errorList.getSelected());
                    GridPos pos = (GridPos) child.getUserObject();
                    if (pos != null) {
                        window.setTextFocus(getHolder(pos.getX(), pos.getY()));
                    }
                }
                getWindowManager().closeWindow(modalWindow);
            }
        });
        panel.children(errorList, button("Close")
                .event(() -> getWindowManager().closeWindow(modalWindow)));

        ProgramCardInstance instance = makeGridInstance(false);

        List<Pair<GridPos, String>> errors = ProgramValidator.validate(instance);
        for (Pair<GridPos, String> entry : errors) {
            GridPos p = entry.getKey();
            errorList.children(label(entry.getValue())
                    .color(0xffff0000)
                    .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .userObject(p));
        }
    }

    private void askNameAndSave(int slot) {
        ItemStack card = tileEntity.getItems().getStackInSlot(slot);
        if (card.isEmpty()) {
            GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, "No card!");
            return;
        }
        String cardName = ProgramCardItem.getCardName(card);
        if (cardName.isEmpty() || McJtyLib.proxy.isSneaking()) {
            GuiTools.askSomething(minecraft, this, getWindowManager(), 50, 50, "Card name:", cardName, s -> {
                saveProgram(slot, s);
            });
        } else {
            saveProgram(slot, cardName);
        }
    }

    private void saveProgram(int slot, String name) {
        ItemStack card = tileEntity.getItems().getStackInSlot(slot);
        if (card.isEmpty()) {
            return;
        }
        if (name != null) {
            ProgramCardItem.setCardName(card, name);
        }
        ProgramCardInstance instance = makeGridInstance(false);
        instance.writeToNBT(card);
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemInventoryProgrammer(tileEntity.getPos(),
                slot, card.getTag()));
    }

    private ProgramCardInstance makeGridInstance(boolean selectionOnly) {
        ProgramCardInstance instance = ProgramCardInstance.newInstance();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                IconHolder holder = getHolder(x, y);
                IIcon icon = holder.getIcon();
                if (icon != null) {
                    if (selectionOnly && !icon.hasOverlay("S")) {
                        continue;
                    }
                    String operandId = icon.getID();
                    GridInstance.Builder builder = GridInstance.builder(operandId);
                    for (Connection connection : Connection.values()) {
                        if (icon.hasOverlay(connection.getId())) {
                            if (connection.isPrimary()) {
                                builder.primaryConnection(connection);
                            } else {
                                builder.secondaryConnection(connection);
                            }
                        }
                    }
                    Opcode opcode = Opcodes.OPCODES.get(operandId);
                    Map<String, Object> data = icon.getData();
                    for (ParameterDescription description : opcode.getParameters()) {
                        ParameterValue value = data == null ? null : (ParameterValue) data.get(description.getName());
                        if (value == null) {
                            value = ParameterValue.constant(null);
                        }
                        Parameter parameter = Parameter.builder().type(description.getType()).value(value).build();
                        builder.parameter(parameter);
                    }

                    instance.putGridInstance(x, y, builder.build());
                }
            }
        }
        return instance;
    }

    private void clearProgram() {
        undoProgram = makeGridInstance(false);
        clearGrid(false);
    }

    private void loadProgram(int slot) {
        ItemStack card = tileEntity.getItems().getStackInSlot(slot);
        if (card.isEmpty()) {
            return;
        }
        clearGrid(false);
        ProgramCardInstance instance = ProgramCardInstance.parseInstance(card);
        if (instance == null) {
            return;
        }

        loadProgram(instance);
    }

    private GridPos getSelectedGridHolder() {
        if (window.getTextFocus() instanceof IconHolder) {
            IconHolder holder = (IconHolder) window.getTextFocus();
            if (holder.getUserObject() instanceof GridPos) {
                return (GridPos) holder.getUserObject();
            }
        }
        return null;
    }

    private void mergeProgram(ProgramCardInstance instance, GridPos pos) {
        // Find the left-most/top-most icon in this program
        GridPos leftTop = new GridPos(10000, 10000);
        int posx;
        int posy;

        if (pos == null) {
            posx = 0;
            posy = 0;
            leftTop = new GridPos(0, 0);
        } else {
            posx = pos.getX();
            posy = pos.getY();

            for (Map.Entry<GridPos, GridInstance> entry : instance.getGridInstances().entrySet()) {
                int x = entry.getKey().getX();
                int y = entry.getKey().getY();
                if (x < leftTop.getX()) {
                    leftTop = entry.getKey();
                } else if (x == leftTop.getX() && y < leftTop.getY()) {
                    leftTop = entry.getKey();
                }
            }
            if (leftTop.getX() > 1000) {
                return; // Nothing to do
            }
        }

        // Check if the program fits in the grid
        for (Map.Entry<GridPos, GridInstance> entry : instance.getGridInstances().entrySet()) {
            int x = entry.getKey().getX() - leftTop.getX() + posx;
            int y = entry.getKey().getY() - leftTop.getY() + posy;
            if (!checkValidGridPos(new GridPos(x, y))) {
                GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "No room for clipboard here!");
                return;
            }
            if (getHolder(x, y).getIcon() != null) {
                GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "No room for clipboard here!");
                return;
            }
        }

        // There is room
        for (Map.Entry<GridPos, GridInstance> entry : instance.getGridInstances().entrySet()) {
            int x = entry.getKey().getX() - leftTop.getX() + posx;
            int y = entry.getKey().getY() - leftTop.getY() + posy;
            loadGridInstance(entry, x, y);
        }
    }

    private void loadProgram(ProgramCardInstance instance) {
        for (Map.Entry<GridPos, GridInstance> entry : instance.getGridInstances().entrySet()) {
            int x = entry.getKey().getX();
            int y = entry.getKey().getY();
            loadGridInstance(entry, x, y);
        }
    }

    private void loadGridInstance(Map.Entry<GridPos, GridInstance> entry, int x, int y) {
        GridInstance gridInstance = entry.getValue();
        IIcon icon = ICONS.get(gridInstance.getId());
        if (icon == null) {
            // Ignore missing icon
            Logging.logError("Opcode with id '" + gridInstance.getId() + "' is missing!");
            return;
        }
        icon = icon.clone();
        if (gridInstance.getPrimaryConnection() != null) {
            icon.addOverlay(CONNECTION_ICONS.get(gridInstance.getPrimaryConnection()));
        }
        if (gridInstance.getSecondaryConnection() != null) {
            icon.addOverlay(CONNECTION_ICONS.get(gridInstance.getSecondaryConnection()));
        }
        Opcode opcode = Opcodes.OPCODES.get(icon.getID());
        List<Parameter> parameters = gridInstance.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            String name = opcode.getParameters().get(i).getName();
            icon.addData(name, parameters.get(i).getParameterValue());
        }

        loading = true;
        getHolder(x, y).setIcon(icon);
        loading = false;
    }

    private Panel setupControlPanel() {
        trashcan = new IconHolder()
                .desiredWidth(14)
                .desiredHeight(14)
                .border(1)
                .borderColor(0xffff0000)
                .tooltips(TextFormatting.YELLOW + "Delete opcode", "Drop opcodes here to", "delete them")
                .selectable(false);
        return horizontal(1, 2).hint(108, 136, 145, 18)
                .children(
                        button("Load")
                                .tooltips(TextFormatting.YELLOW + "Load program", "Load the current program", "from a program card")
                                .desiredHeight(15).event(() -> loadProgram(ProgrammerContainer.SLOT_CARD)),
                        button("Save")
                                .tooltips(TextFormatting.YELLOW + "Save program",
                                        "Save the current program",
                                        "to a program card",
                                        TextFormatting.GREEN + "Press shift to change name")
                                .desiredHeight(15).event(() -> askNameAndSave(ProgrammerContainer.SLOT_CARD)),
                        button("Clear")
                                .tooltips(TextFormatting.YELLOW + "Clear program", "Remove all opcodes on the grid", "(press Ctrl-Z if this was a mistake)")
                                .desiredHeight(15).event(this::clearProgram),
                        button("Val")
                                .tooltips(TextFormatting.YELLOW + "Validate program", "Perform some basic validations on", "the current program", "Double click on error", "to highlight opcode")
                                .desiredHeight(15)
                                .event(this::validateProgram),
                        trashcan);
    }

    private void clearCategoryLabels() {
        for (ImageChoiceLabel label : categoryLabels) {
            label.setCurrentChoice("off");
        }
        currentCategory = null;
        fillOpcodes();
    }

    private void makeCategoryToggle(Panel panel, int cx, int cy, OpcodeCategory category, int u, int v) {
        ImageChoiceLabel catLabel = new ImageChoiceLabel()
                .hint(cx * 18 + 3, cy * 18 + 14, 16, 16)
                .choice("off", "Filter on category " + category.getName() + " (off)", guiElements, u * 16, v * 16)
                .choice("on", "Filter on category " + category.getName() + " (on)", guiElements, u * 16 + 16, v * 16);
        catLabel.event((newChoice) -> {
            if ("on".equals(newChoice)) {
                clearCategoryLabels();
                catLabel.setCurrentChoice("on");
                currentCategory = category;
                fillOpcodes();
            } else {
                clearCategoryLabels();
            }
        });
        panel.children(catLabel);
        categoryLabels.add(catLabel);
    }

    private Panel setupListPanel() {
        Panel panel = positional()
                .hint(2, 2, 78, 232)
                .children(label(0, 0, 70, 12, "Opcodes:"));

        makeCategoryToggle(panel, 0, 0, OpcodeCategory.CATEGORY_ITEMS, 8, 5);
        makeCategoryToggle(panel, 1, 0, OpcodeCategory.CATEGORY_LIQUIDS, 10, 5);
        makeCategoryToggle(panel, 2, 0, OpcodeCategory.CATEGORY_CRAFTING, 6, 5);
        makeCategoryToggle(panel, 3, 0, OpcodeCategory.CATEGORY_REDSTONE, 14, 5);
        makeCategoryToggle(panel, 0, 1, OpcodeCategory.CATEGORY_ENERGY, 12, 5);
        makeCategoryToggle(panel, 1, 1, OpcodeCategory.CATEGORY_NUMBERS, 8, 6);
        makeCategoryToggle(panel, 2, 1, OpcodeCategory.CATEGORY_VECTORS, 10, 6);
        makeCategoryToggle(panel, 3, 1, OpcodeCategory.CATEGORY_GRAPHICS, 6, 6);

        opcodeList = list(0, 52, 68, 180)
                .name("opcodes")
                .propagateEventsToChildren(true)
                .invisibleSelection(true)
                .drawHorizontalLines(false)
                .rowheight(ICONSIZE + 2);
        Slider slider = slider(68, 52, 8, 180)
                .vertical()
                .scrollableName("opcodes");

        fillOpcodes();

        return panel.children(opcodeList, slider);
    }

    private void fillOpcodes() {
        opcodeList.removeChildren();
        int x = 0;
        int y = 0;
        Panel childPanel = null;
        for (Opcode opcode : Opcodes.SORTED_OPCODES) {
            if (currentCategory != null) {
                if (!opcode.getCategories().contains(currentCategory)) {
                    continue;
                }
            }
            String key = opcode.getId();
            if (childPanel == null) {
                childPanel = new Panel().layout(new HorizontalLayout().setVerticalMargin(1).setSpacing(1).setHorizontalMargin(0)).desiredHeight(ICONSIZE + 1);
                opcodeList.children(childPanel);
            }
            IconHolder holder = new IconHolder() {
                @Override
                public List<String> getTooltips() {
                    return getIconTooltip(getIcon());
                }
            }
                    .desiredWidth(ICONSIZE).desiredHeight(ICONSIZE)
                    .makeCopy(true);
            holder.setIcon(ICONS.get(key).clone());
            childPanel.children(holder);
            x++;
            if (x >= 3) {
                y++;
                x = 0;
                childPanel = null;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleClipboard(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (handleClipboard(keyCode)) {
            return;
        }
        super.keyTypedFromEvent(keyCode, keyCode);
    }

    private boolean handleClipboard(int keyCode) {
        if (McJtyLib.proxy.isCtrlKeyDown()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                selectAll();
            } else if (keyCode == GLFW.GLFW_KEY_C) {
                if (!checkSelection()) {
                    GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Nothing is selected!");
                } else {
                    ProgramCardInstance instance = makeGridInstance(true);
                    String json = instance.writeToJson();
                    try {
                        StringSelection selection = new StringSelection(json);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                    } catch (Exception e) {
                        GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Error copying to clipboard!");
                    }
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_Z) {
                if (undoProgram != null) {
                    ProgramCardInstance curProgram = makeGridInstance(false);
                    clearGrid(false);
                    loadProgram(undoProgram);
                    undoProgram = curProgram;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_X) {
                if (!checkSelection()) {
                    GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Nothing is selected!");
                } else {
                    ProgramCardInstance instance = makeGridInstance(true);
                    String json = instance.writeToJson();
                    try {
                        StringSelection selection = new StringSelection(json);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                        undoProgram = makeGridInstance(false);
                        clearGrid(checkSelection());
                    } catch (Exception e) {
                        GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Error copying to clipboard!");
                    }
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    String data = (String) clipboard.getData(DataFlavor.stringFlavor);
                    ProgramCardInstance program = ProgramCardInstance.readFromJson(data);
                    undoProgram = makeGridInstance(false);
                    mergeProgram(program, getSelectedGridHolder());
                } catch (UnsupportedFlavorException e) {
                    GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Clipboard does not contain program!");
                } catch (Exception e) {
                    GuiTools.showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Error reading from clipboard!");
                }
            }
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotId == -999) {
            return;
        }
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    private List<String> getIconTooltipGrid(int x, int y) {
        IconHolder holder = getHolder(x, y);
        IIcon icon = holder.getIcon();
        if (icon != null) {
            Opcode opcode = Opcodes.OPCODES.get(icon.getID());
            List<String> description = opcode.getDescription();
            List<String> tooltips = new ArrayList<>();
            boolean isComment = Opcodes.DO_COMMENT.getId().equals(opcode.getId());
            if (isComment) {
                Map<String, Object> data = icon.getData() == null ? Collections.emptyMap() : icon.getData();
                for (ParameterDescription parameter : opcode.getParameters()) {
                    String name = parameter.getName();
                    ParameterValue value = (ParameterValue) data.get(name);
                    if (value != null) {
                        String s = ParameterTypeTools.stringRepresentation(parameter.getType(), value);
                        if (!s.isEmpty()) {
                            tooltips.add(TextFormatting.BLUE + s);
                        }
                    }
                }
            } else if (McJtyLib.proxy.isSneaking()) {
                tooltips.add(description.get(0) + TextFormatting.WHITE + " [" + x + "," + y + "]");
                Map<String, Object> data = icon.getData() == null ? Collections.emptyMap() : icon.getData();
                for (ParameterDescription parameter : opcode.getParameters()) {
                    String name = parameter.getName();
                    ParameterValue value = (ParameterValue) data.get(name);
                    if (value != null) {
                        tooltips.add(TextFormatting.BLUE + "Par " + name + ": " + ParameterTypeTools.stringRepresentation(parameter.getType(), value));
                    } else {
                        tooltips.add(TextFormatting.BLUE + "Par " + name + ": NULL");
                    }
                }
            } else {
                tooltips.add(description.get(0));
                tooltips.add("<Shift for more info>");
            }
            return tooltips;
        }
        return Collections.emptyList();
    }

    private List<String> getIconTooltip(IIcon icon) {
        if (icon != null) {
            Opcode opcode = Opcodes.OPCODES.get(icon.getID());
            List<String> description = opcode.getDescription();
            List<String> tooltips = new ArrayList<>();
            if (McJtyLib.proxy.isSneaking()) {
                tooltips.addAll(description);
                for (ParameterDescription parameter : opcode.getParameters()) {
                    boolean first = true;
                    for (int i = 0; i < parameter.getDescription().size(); i++) {
                        String s = parameter.getDescription().get(i);
                        if (first) {
                            s = TextFormatting.BLUE + "Par '" + parameter.getName() + "': " + s;
                            first = false;
                        } else {
                            s = TextFormatting.BLUE + "      " + s;
                        }
                        if (parameter.isOptional() && i == parameter.getDescription().size() - 1) {
                            s += TextFormatting.GOLD + " [Optional]";
                        }
                        tooltips.add(s);
                    }
                }
                tooltips.add(TextFormatting.YELLOW + "Result: " + opcode.getOutputDescription());
            } else {
                tooltips.add(description.get(0));
                tooltips.add("<Shift for more info>");
            }
            return tooltips;
        }
        return Collections.emptyList();
    }

    private void selectIcon(Window parent, Widget<?> focused) {
        if (parent == window && focused instanceof IconHolder) {
            clearEditorPanel();
            IconHolder iconHolder = (IconHolder) focused;
            IIcon icon = iconHolder.getIcon();
            if (icon != null) {
                setEditorPanel(iconHolder, icon);
            }
        }
    }

    private Panel createValuePanel(ParameterDescription parameter, IIcon icon, IconHolder iconHolder, String tempDefault, boolean constantOnly) {
        Label label = label(0, 0, 53, 13, StringUtils.capitalize(parameter.getName()) + ":")
                .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                .desiredHeight(13);
        List<String> description = new ArrayList<>(parameter.getDescription());
        if (parameter.isOptional()) {
            description.set(description.size() - 1, description.get(description.size() - 1) + TextFormatting.GOLD + " [Optional]");
        }
        if (tempDefault != null && !tempDefault.isEmpty()) {
            description.add(TextFormatting.BLUE + tempDefault);
        }

        String[] tooltips = description.toArray(new String[description.size()]);
        TextField field = textfield(0, 12, 68, 13)
                .text(tempDefault)
                .tooltips(tooltips)
                .desiredHeight(13)
                .editable(false);
        Button button = button(58, 0, 11, 13, "...")
                .desiredHeight(13)
                .tooltips(tooltips)
                .event(() -> openValueEditor(icon, iconHolder, parameter, field, constantOnly));

        return positional().children(label, field, button)
                .desiredWidth(68);
    }

    private void openValueEditor(IIcon icon, IconHolder iconHolder, ParameterDescription parameter, TextField field, boolean constantOnly) {
        ParameterEditor editor = ParameterEditors.getEditor(parameter.getType());
        Panel editPanel;
        if (editor != null) {
            editPanel = positional()
                    .filledRectThickness(1);
            Map<String, Object> data = icon.getData() == null ? Collections.emptyMap() : icon.getData();
            editor.build(minecraft, this, editPanel, o -> {
                icon.addData(parameter.getName(), o);
                field.text(ParameterTypeTools.stringRepresentation(parameter.getType(), o));
            });
            editor.writeValue((ParameterValue) data.get(parameter.getName()));
            if (constantOnly) {
                editor.constantOnly();
            }
        } else {
            return;
        }

        Panel panel = vertical()
                .filledBackground(0xff666666, 0xffaaaaaa)
                .filledRectThickness(1);
        panel.bounds(50, 25, 200, 60 + editor.getHeight());
        Window modalWindow = getWindowManager().createModalWindow(panel);
        panel.children(label(StringUtils.capitalize(parameter.getName()) + ":"),
                editPanel, button("Close")
                        .event(() -> {
                            getWindowManager().closeWindow(modalWindow);
                            window.setTextFocus(iconHolder);
                        }));
        editor.initialFocus(modalWindow);
        editor.setOnClose(() -> window.setTextFocus(iconHolder));
    }

    private void clearEditorPanel() {
        editorList.removeChildren();
    }

    private void setEditorPanel(IconHolder iconHolder, IIcon icon) {
        String id = icon.getID();
        Opcode opcode = Opcodes.OPCODES.get(id);
        Map<String, Object> data = icon.getData() == null ? Collections.emptyMap() : icon.getData();
        clearEditorPanel();
        for (ParameterDescription parameter : opcode.getParameters()) {
            String name = parameter.getName();
            ParameterValue value = (ParameterValue) data.get(name);
            Panel panel;
            if (value != null) {
                panel = createValuePanel(parameter, icon, iconHolder, ParameterTypeTools.stringRepresentation(parameter.getType(), value), opcode.isEvent());
            } else {
                panel = createValuePanel(parameter, icon, iconHolder, "", opcode.isEvent());
            }
            editorList.children(panel);
        }
    }

    private Panel setupEditorPanel() {
        editorList = list(0, 0, 75, HEIGHT - 137 - 3)
                .name("editor")
                .propagateEventsToChildren(true)
                .rowheight(30);
        Slider slider = slider(76, 0, 9, HEIGHT - 137 - 3)
                .scrollableName("editor");
        return positional().hint(4, 137, 85, HEIGHT - 137 - 3)
                .filledRectThickness(-1)
                .filledBackground(StyleConfig.colorListBackground)
                .children(editorList, slider);
    }

    private int saveCounter = 10;

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();

        trashcan.setIcon(null);
        saveCounter--;
        if (saveCounter < 0) {
            saveCounter = 10;
            validateAndHilight();
            saveProgram(ProgrammerContainer.SLOT_DUMMY, null);
        }
    }
}
