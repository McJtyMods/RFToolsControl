package mcjty.rftoolscontrol.modules.multitank.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.VerticalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankContainer;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.label;
import static mcjty.lib.gui.widgets.Widgets.positional;
import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.TANKS;

public class GuiMultiTank extends GenericGuiContainer<MultiTankTileEntity, MultiTankContainer> {

    public static final int WIDTH = 180;
    public static final int HEIGHT = 87;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsControl.MODID, "textures/gui/tank.png");
    private int listDirty = 0;

    private BlockRender liquids[] = new BlockRender[TANKS];
    private Label labels[] = new Label[TANKS];

    public GuiMultiTank(MultiTankTileEntity te, MultiTankContainer container, PlayerInventory inventory) {
        super(te, container, inventory, /*@todo 1.15 GuiProxy.GUI_MANUAL_CONTROL*/ ManualEntry.EMPTY);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register() {
        register(MultiTankModule.MULTITANK_CONTAINER.get(), GuiMultiTank::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = positional().background(iconLocation);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);

        for (int i = 0 ; i < TANKS ; i++) {
            liquids[i] = new BlockRender()
                    .hint(10, 9 + i * 18, 16, 16);
            toplevel.children(liquids[i]);
            labels[i] = label(32, 9 + i * 18, WIDTH-32-6, 16, "")
                    .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .verticalAlignment(VerticalAlignment.ALIGN_CENTER);
            toplevel.children(labels[i]);
        }

        window = new Window(this, toplevel);
    }

    private void requestLists() {
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetListFromServer(tileEntity.getBlockPos(), MultiTankTileEntity.CMD_GETFLUIDS.getName()));
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestLists();
            listDirty = 10;
        }
    }
    private void updateLiquids() {
        requestListsIfNeeded();
        MultiTankFluidProperties[] properties = tileEntity.getProperties();
        for (int i = 0 ; i < TANKS ; i++) {
            if (i < properties.length && properties[i] != null) {
                FluidStack stack = properties[i].getContents();
                if (!stack.isEmpty()) {
                    liquids[i].renderItem(stack);
                    labels[i].text(stack.getDisplayName().getString() /* was getFormattedText() */ + " (" + stack.getAmount() + "mb)");
                    continue;
                }
            }
            liquids[i].renderItem(null);
            labels[i].text("");
        }
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        updateLiquids();
        drawWindow(matrixStack);
    }
}
