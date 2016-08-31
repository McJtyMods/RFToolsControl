package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiProcessor extends GenericGuiContainer<ProcessorTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int WIDTH = 256;
    public static final int HEIGHT = 236;

    public static int ICONSIZE = 20;

    private static final ResourceLocation mainBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/processor.png");
    private static final ResourceLocation sideBackground = new ResourceLocation(RFToolsControl.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation icons = new ResourceLocation(RFToolsControl.MODID, "textures/gui/icons.png");

    private Window sideWindow;
    private EnergyBar energyBar;
    private ToggleButton[] setupButtons = new ToggleButton[ProcessorContainer.CARD_SLOTS];

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
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(122, 8, 70, 10)).setShowText(false).setHorizontal();
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        toplevel.addChild(energyBar);

        for (int i = 0 ; i < ProcessorContainer.CARD_SLOTS ; i++) {
            setupButtons[i] = new ToggleButton(mc, this)
                .addButtonEvent(this::setupMode)
                .setLayoutHint(new PositionalLayout.PositionalHint(11 + i * 18, 6, 15, 7));
            toplevel.addChild(setupButtons[i]);
        }
        window = new Window(this, toplevel);

        // --- Side window ---
        Panel listPanel = setupListPanel();
        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground)
                .addChild(listPanel);
        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);
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
            int leftx = window.getToplevel().getBounds().x;
            if (x >= leftx+10 && x <= leftx+10+ProcessorContainer.CARD_SLOTS*18 && y >= 6 && y <= 6+7) {
                super.mouseClicked(x, y, button);
            }
        }
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
        mgr.getIconManager().setClickHoldToDrag(true);
    }

    private Panel setupListPanel() {
        WidgetList list = new WidgetList(mc, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(0, 0, 62, 220))
                .setPropagateEventsToChildren(true)
                .setInvisibleSelection(true)
                .setDrawHorizontalLines(false)
                .setRowheight(ICONSIZE+2);
        Slider slider = new Slider(mc, this)
                .setVertical()
                .setScrollable(list)
                .setLayoutHint(new PositionalLayout.PositionalHint(62, 0, 9, 220));

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(5, 5, 72, 220))
                .addChild(list)
                .addChild(slider);
//                .setFilledRectThickness(-2)
//                .setFilledBackground(StyleConfig.colorListBackground);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
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

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) guiLeft, (float) guiTop, 0.0F);
        GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) (short) 240 / 1.0F, (float) (short) 240 / 1.0F);

//        ItemStack[] ghostSlots = tileEntity.getGhostSlots();
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

//        for (int i = 0 ; i < ghostSlots.length ; i++) {
//            ItemStack stack = ghostSlots[i];
//            if (stack != null) {
//                int slotIdx;
//                if (i < CrafterContainer.BUFFER_SIZE) {
//                    slotIdx = i + CrafterContainer.SLOT_BUFFER;
//                } else {
//                    slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
//                }
//                Slot slot = inventorySlots.getSlot(slotIdx);
//                if (!slot.getHasStack()) {
//                    itemRender.renderItemAndEffectIntoGUI(stack, slot.xDisplayPosition, slot.yDisplayPosition);
//
//                    GlStateManager.disableLighting();
//                    GlStateManager.enableBlend();
//                    GlStateManager.disableDepth();
//                    this.mc.getTextureManager().bindTexture(iconGuiElements);
//                    RenderHelper.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 14 * 16, 3 * 16, 16, 16);
//                    GlStateManager.enableDepth();
//                    GlStateManager.disableBlend();
//                    GlStateManager.enableLighting();
//                }
//            }
//
//        }
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;

        GlStateManager.popMatrix();
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    }
}
