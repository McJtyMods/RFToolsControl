package mcjty.rftoolscontrol.blocks.programmer;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.icons.IIcon;
import mcjty.lib.gui.icons.ImageIcon;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.PacketUpdateNBTItemInventory;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.Connection;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.editors.ParameterEditor;
import mcjty.rftoolscontrol.logic.editors.ParameterEditors;
import mcjty.rftoolscontrol.logic.grid.GridInstance;
import mcjty.rftoolscontrol.logic.grid.GridPos;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.*;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiProgrammer extends GenericGuiContainer<ProgrammerTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    public static final int GRID_HEIGHT = 10;
    public static final int GRID_WIDTH = 11;

    public static int ICONSIZE = 20;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/programmer.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");

    private Window sideWindow;
    private WidgetList gridList;
    private WidgetList editorList;
    private IconHolder trashcan;

    private int iconLeavesFromX = -1;
    private int iconLeavesFromY = -1;
    private boolean loading = false;

    private static final Map<String, IIcon> ICONS = new HashMap<>();
    private static final Map<Connection, IIcon> CONNECTION_ICONS = new HashMap<>();

    static {
        CONNECTION_ICONS.put(Connection.UP, new ImageIcon(Connection.UP.getId()).setImage(icons, 0*ICONSIZE, 5*ICONSIZE));
        CONNECTION_ICONS.put(Connection.UP_NEG, new ImageIcon(Connection.UP_NEG.getId()).setImage(icons, 0*ICONSIZE, 6*ICONSIZE));
        CONNECTION_ICONS.put(Connection.RIGHT, new ImageIcon(Connection.RIGHT.getId()).setImage(icons, 1*ICONSIZE, 5*ICONSIZE));
        CONNECTION_ICONS.put(Connection.RIGHT_NEG, new ImageIcon(Connection.RIGHT_NEG.getId()).setImage(icons, 1*ICONSIZE, 6*ICONSIZE));
        CONNECTION_ICONS.put(Connection.DOWN, new ImageIcon(Connection.DOWN.getId()).setImage(icons, 2*ICONSIZE, 5*ICONSIZE));
        CONNECTION_ICONS.put(Connection.DOWN_NEG, new ImageIcon(Connection.DOWN_NEG.getId()).setImage(icons, 2*ICONSIZE, 6*ICONSIZE));
        CONNECTION_ICONS.put(Connection.LEFT, new ImageIcon(Connection.LEFT.getId()).setImage(icons, 3*ICONSIZE, 5*ICONSIZE));
        CONNECTION_ICONS.put(Connection.LEFT_NEG, new ImageIcon(Connection.LEFT_NEG.getId()).setImage(icons, 3*ICONSIZE, 6*ICONSIZE));
        for (IIcon icon : CONNECTION_ICONS.values()) {
            ((ImageIcon)icon).setDimensions(ICONSIZE, ICONSIZE);
        }
    }

    public GuiProgrammer(ProgrammerTileEntity tileEntity, ProgrammerContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, tileEntity, container, RFToolsControl.GUI_MANUAL_CONTROL, "programmer");

        xSize = WIDTH;
        ySize = HEIGHT;

        initIcons();
    }

    private void initIcons() {
        if (ICONS.isEmpty()) {
            for (Map.Entry<String, Opcode> entry : Opcodes.OPCODES.entrySet()) {
                String id = entry.getKey();
                Opcode opcode = entry.getValue();
                ICONS.put(id, new ImageIcon(id).setDimensions(ICONSIZE, ICONSIZE).setImage(icons, opcode.getIconU()*ICONSIZE, opcode.getIconV()*ICONSIZE));
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        // --- Main window ---
        Panel editorPanel = setupEditorPanel();
        Panel controlPanel = setupControlPanel();
        Panel gridPanel = setupGridPanel();
        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(mainBackground)
                .addChild(editorPanel)
                .addChild(controlPanel)
                .addChild(gridPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel).addFocusEvent((parent, focused) -> selectIcon(parent, focused));

        // --- Side window ---
        Panel listPanel = setupListPanel();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground)
                .addChild(listPanel);
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);

        loadProgram(ProgrammerContainer.SLOT_DUMMY);
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupGridPanel() {

        Panel panel = new Panel(mc, this).setLayout(new PositionalLayout())
                .setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 246, 130));

        gridList = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 236, 130))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setRowheight(ICONSIZE+1);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(gridList)
                .setLayoutHint(new PositionalLayout.PositionalHint(237, 0, 9, 130));

        for (int y = 0; y < GRID_HEIGHT; y++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(-1).setHorizontalMargin(0).setVerticalMargin(0));
            for (int x = 0; x < GRID_WIDTH; x++) {
                int finalX = x;
                int finalY = y;
                IconHolder holder = new IconHolder(mc, this) {
                    @Override
                    public List<String> getTooltips() {
                        return getIconTooltip(getIcon());
                    }
                }
                        .setDesiredWidth(ICONSIZE+2)
                        .setDesiredHeight(ICONSIZE+2)
                        .setBorder(1)
                        .setBorderColor(0xff777777)
                        .setSelectable(true)
                        .addIconLeavesEvent(((parent, icon) -> {
                            iconLeavesFromX = finalX;
                            iconLeavesFromY = finalY;
                            return true;
                        }))
                        .addIconArrivesEvent(((parent, icon) -> {
                            if (icon != null && !loading) {
                                handleNewIconOverlay(icon, finalX, finalY);
                            }
                            return true;
                        }))
                        .addIconClickedEvent((parent, icon, dx, dy) -> {
                            if (dy <= 5 && dx >= 7 && dx <= 14) {
                                handleIconOverlay(icon, Connection.UP);
                            } else if (dy >= ICONSIZE-3 && dx >= 7 && dx <= 14) {
                                handleIconOverlay(icon, Connection.DOWN);
                            } else if (dx <= 5 && dy >= 7 && dy <= 14) {
                                handleIconOverlay(icon, Connection.LEFT);
                            } else if (dx >= ICONSIZE-3 && dy >= 7 && dy <= 14) {
                                handleIconOverlay(icon, Connection.RIGHT);
                            }
                            System.out.println("dx = " + dx + "," + dy);
                            return true;
                        });
                rowPanel.addChild(holder);
            }
            gridList.addChild(rowPanel);
        }

//        int leftx = 0;
//        int topy = 0;
//        for (int x = 0 ; x < 13 ; x++) {
//            for (int y = 0 ; y < 6 ; y++) {
//                IconHolder holder = new IconHolder(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(leftx + x*19, topy + y*19, 18, 18)).setBorder(1);
//                panel.addChild(holder);
//            }
//        }

        panel.addChild(gridList).addChild(slider);

        return panel;
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
        tryConnectToThis(x-1, y, icon, Connection.RIGHT);
        tryConnectToThis(x+1, y, icon, Connection.LEFT);
        tryConnectToThis(x, y-1, icon, Connection.DOWN);
        tryConnectToThis(x, y+1, icon, Connection.UP);
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
            if (icon.hasOverlay(connection.getId())) {
                icon.clearOverlays();
            } else {
                icon.clearOverlays();
                icon.addOverlay(CONNECTION_ICONS.get(connection));
            }
        } else {
            if (icon.hasOverlay(connection.getId())) {
                icon.removeOverlay(connection.getId());
                icon.addOverlay(CONNECTION_ICONS.get(connection.getOpposite()));
            } else if (icon.hasOverlay(connection.getOpposite().getId())) {
                icon.removeOverlay(connection.getOpposite().getId());
            } else {
                icon.addOverlay(CONNECTION_ICONS.get(connection));
            }
        }
    }

    private void clearGrid() {
        for (int x = 0 ; x < GRID_WIDTH ; x++) {
            for (int y = 0 ; y < GRID_HEIGHT ; y++) {
                getHolder(x, y).setIcon(null);
            }
        }
    }

    private IconHolder getHolder(int x, int y) {
        Panel row = (Panel) gridList.getChild(y);
        return (IconHolder) row.getChild(x);
    }

    private void validateProgram() {
        Panel panel = new Panel(mc, this)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        panel.setBounds(new Rectangle(60, 10, 200, 130));
        Window modalWindow = getWindowManager().createModalWindow(panel);
        WidgetList errors = new WidgetList(mc, this);
        panel.addChild(errors);
        panel.addChild(new Button(mc, this)
                .addButtonEvent(w ->  {
                    getWindowManager().closeWindow(modalWindow);
                })
                .setText("Close"));
        ProgramCardInstance instance = makeGridInstance();

        // @todo, move this code to a validator class

        Map<GridPos, GridInstance> grid = instance.getGridInstances();

        // Find all unreachable instances.
        Set<GridPos> reachableLocations = new HashSet<>();
        for (Map.Entry<GridPos, GridInstance> entry : grid.entrySet()) {
            GridInstance g = entry.getValue();
            if (g.getPrimaryConnection() != null) {
                reachableLocations.add(g.getPrimaryConnection().offset(entry.getKey()));
            }
            if (g.getSecondaryConnection() != null) {
                reachableLocations.add(g.getSecondaryConnection().offset(entry.getKey()));
            }
        }
        for (Map.Entry<GridPos, GridInstance> entry : grid.entrySet()) {
            GridInstance g = entry.getValue();
            Opcode opcode = Opcodes.OPCODES.get(g.getId());
            GridPos p = entry.getKey();
            if (!opcode.isEvent() && !reachableLocations.contains(p)) {
                errors.addChild(new Label(mc, this)
                        .setColor(0xffff0000)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setText("Unreachable: " + p.getX() + "," + p.getY()));
            }
        }

    }

    private void saveProgram(int slot) {
        ItemStack card = tileEntity.getStackInSlot(slot);
        if (card == null) {
            return;
        }
        ProgramCardInstance instance = makeGridInstance();
        instance.writeToNBT(card);
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemInventory(tileEntity.getPos(),
                slot, card.getTagCompound()));
    }

    private ProgramCardInstance makeGridInstance() {
        ProgramCardInstance instance = ProgramCardInstance.newInstance();
        for (int x = 0 ; x < GRID_WIDTH ; x++) {
            for (int y = 0 ; y < GRID_HEIGHT ; y++) {
                IconHolder holder = getHolder(x, y);
                IIcon icon = holder.getIcon();
                if (icon != null) {
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
                    if (data != null) {
                        for (ParameterDescription description : opcode.getParameters()) {
                            ParameterValue value = (ParameterValue) data.get(description.getName());
                            if (value == null) {
                                value = ParameterValue.constant(null);
                            }
                            Parameter parameter = Parameter.builder().type(description.getType()).value(value).build();
                            builder.parameter(parameter);
                        }
                    }

                    instance.putGridInstance(x, y, builder.build());
                }
            }
        }
        return instance;
    }

    private void clearProgram() {
        clearGrid();
    }

    private void loadProgram(int slot) {
        ItemStack card = tileEntity.getStackInSlot(slot);
        if (card == null) {
            return;
        }
        clearGrid();
        ProgramCardInstance instance = ProgramCardInstance.parseInstance(card);
        if (instance == null) {
            return;
        }
        for (Map.Entry<GridPos, GridInstance> entry : instance.getGridInstances().entrySet()) {
            int x = entry.getKey().getX();
            int y = entry.getKey().getY();
            GridInstance gridInstance = entry.getValue();
            IIcon icon = ICONS.get(gridInstance.getId());
            if (icon == null) {
                // Ignore missing icon
                Logging.logError("Opcode with id '" + gridInstance.getId() + "' is missing!");
                continue;
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
            for (int i = 0 ; i < parameters.size() ; i++) {
                String name = opcode.getParameters().get(i).getName();
                icon.addData(name, parameters.get(i).getParameterValue());
            }
//            for (Parameter parameter : parameters) {
//                String name = parameter.getParameterDescription().getName();
//                icon.addData(name, parameter.getParameterValue());
//            }

            loading = true;
            getHolder(x, y).setIcon(icon);
            loading = false;
        }
    }

    private Panel setupControlPanel() {
        trashcan = new IconHolder(mc, this)
                .setDesiredWidth(14)
                .setDesiredHeight(14)
                .setBorder(1)
                .setBorderColor(0xffff0000)
                .setTooltips("Drop opcodes here to", "delete them")
                .setSelectable(false);
        return new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(2).setHorizontalMargin(1)).setLayoutHint(new PositionalLayout.PositionalHint(108, 136, 145, 18))
                .addChild(new Button(mc, this).setText("Load").setDesiredHeight(15).addButtonEvent(w -> loadProgram(ProgrammerContainer.SLOT_CARD)))
                .addChild(new Button(mc, this).setText("Save").setDesiredHeight(15).addButtonEvent(w -> saveProgram(ProgrammerContainer.SLOT_CARD)))
                .addChild(new Button(mc, this).setText("Clear").setDesiredHeight(15).addButtonEvent(w -> clearProgram()))
                .addChild(new Button(mc, this).setText("Val").setDesiredHeight(15).addButtonEvent(w -> validateProgram()))
                .addChild(trashcan);
    }

    private Panel setupListPanel() {
        WidgetList list = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 62, 226))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setRowheight(ICONSIZE+2);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(list)
                .setLayoutHint(new PositionalLayout.PositionalHint(62, 0, 9, 226));

        int x = 0;
        int y = 0;
        Panel childPanel = null;
        for (Opcode opcode : Opcodes.SORTED_OPCODES) {
            String key = opcode.getId();
            if (childPanel == null) {
                childPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setVerticalMargin(1).setSpacing(3).setHorizontalMargin(3)).setDesiredHeight(ICONSIZE+1);
                list.addChild(childPanel);
            }
            IconHolder holder = new IconHolder(mc, this) {
                @Override
                public List<String> getTooltips() {
                    return getIconTooltip(getIcon());
                }
            }
                    .setDesiredWidth(ICONSIZE).setDesiredHeight(ICONSIZE)
                    .setMakeCopy(true);
            holder.setIcon(ICONS.get(key).clone());
            childPanel.addChild(holder);
            x++;
            if (x >= 2) {
                y++;
                x = 0;
                childPanel = null;
            }
        }

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 72, 226))
                .addChild(list)
                .addChild(slider);
    }

    private List<String> getIconTooltip(IIcon icon) {
        if (icon != null) {
            Opcode opcode = Opcodes.OPCODES.get(icon.getID());
            List<String> description = opcode.getDescription();
            List<String> tooltips = new ArrayList<>();
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                tooltips.addAll(description);
                for (ParameterDescription parameter : opcode.getParameters()) {
                    boolean first = true;
                    for (String s : parameter.getDescription()) {
                        if (first) {
                            tooltips.add(TextFormatting.BLUE + "Par '" + parameter.getName() + "': " + s);
                            first = false;
                        } else {
                            tooltips.add(TextFormatting.BLUE + "      " + s);
                        }
                    }
                }
                tooltips.add(TextFormatting.YELLOW + "Result: " + opcode.getOutputDescription());
                return tooltips;
            } else {
                tooltips.add(description.get(0));
                tooltips.add("<Shift for more info>");
                return tooltips;
            }
        }
        return Collections.emptyList();
    }

    private void selectIcon(Window parent, Widget focused) {
        if (parent == window && focused instanceof IconHolder) {
            clearEditorPanel();
            IconHolder iconHolder = (IconHolder) focused;
            IIcon icon = iconHolder.getIcon();
            if (icon != null) {
                setEditorPanel(iconHolder, icon);
            }
        }
    }

    private Panel createValuePanel(ParameterDescription parameter, IIcon icon, IconHolder iconHolder, String tempDefault) {
        Label label = (Label) new Label(mc, this)
                .setText(StringUtils.capitalize(parameter.getName()) + ":")
                .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                .setDesiredHeight(13)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 60, 13));
        TextField field = new TextField(mc, this)
                .setText("<" + tempDefault + ">")
                .setTooltips(parameter.getDescription().toArray(new String[parameter.getDescription().size()]))
                .setDesiredHeight(13)
                .setEnabled(false)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 12, 54, 13));
        Button button = new Button(mc, this)
                .setText("...")
                .setDesiredHeight(13)
                .setTooltips(parameter.getDescription().toArray(new String[parameter.getDescription().size()]))
                .addButtonEvent(w -> openValueEditor(icon, iconHolder, parameter, field))
                .setLayoutHint(new PositionalLayout.PositionalHint(55, 12, 11, 13));

        return new Panel(mc, this).setLayout(new PositionalLayout())
                .addChild(label)
                .addChild(field)
                .addChild(button)
                .setDesiredWidth(68);
    }

    private void openValueEditor(IIcon icon, IconHolder iconHolder, ParameterDescription parameter, TextField field) {
        ParameterEditor editor = ParameterEditors.getEditor(parameter.getType());
        Panel editPanel;
        if (editor != null) {
            editPanel = new Panel(mc, this).setLayout(new PositionalLayout())
                    .setFilledRectThickness(1);
            Map<String, Object> data = icon.getData() == null ? Collections.emptyMap() : icon.getData();
            editor.build(mc, this, editPanel, o -> {
                icon.addData(parameter.getName(), o);
                field.setText("<" + parameter.getType().stringRepresentation(o) + ">");
            });
            editor.writeValue((ParameterValue)data.get(parameter.getName()));
        } else {
            return;
        }

        Panel panel = new Panel(mc, this)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        panel.setBounds(new Rectangle(50, 50, 200, 60 + editor.getHeight()));
        Window modalWindow = getWindowManager().createModalWindow(panel);
        panel.addChild(new Label(mc, this).setText(StringUtils.capitalize(parameter.getName()) + ":"));
        panel.addChild(editPanel);
        panel.addChild(new Button(mc, this)
                .addButtonEvent(w ->  {
                    getWindowManager().closeWindow(modalWindow);
                    window.setTextFocus(iconHolder);
                })
                .setText("Close"));
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
                panel = createValuePanel(parameter, icon, iconHolder, parameter.getType().stringRepresentation(value));
            } else {
                panel = createValuePanel(parameter, icon, iconHolder, "");
            }
            editorList.addChild(panel);
        }
    }

    private Panel setupEditorPanel() {
        editorList = new WidgetList(mc, this)
                .setPropagateEventsToChildren(true)
                .setRowheight(30)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 75, HEIGHT-137-3));
        Slider slider = new Slider(mc, this).setScrollable(editorList)
                .setLayoutHint(new PositionalLayout.PositionalHint(76, 0, 9, HEIGHT-137-3));
        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(4, 137, 85, HEIGHT-137-3))
                .setFilledRectThickness(-1)
                .setFilledBackground(StyleConfig.colorListBackground)
                .addChild(editorList)
                .addChild(slider);
    }

    private int saveCounter = 10;

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
        trashcan.setIcon(null);
        saveCounter--;
        if (saveCounter < 0) {
            saveCounter = 10;
            saveProgram(ProgrammerContainer.SLOT_DUMMY);
        }
    }
}
