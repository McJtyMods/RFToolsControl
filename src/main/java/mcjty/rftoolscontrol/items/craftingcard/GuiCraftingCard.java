package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.Argument;
import mcjty.lib.network.Arguments;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.PacketItemNBTToServer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import static mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer.*;


public class GuiCraftingCard extends GenericGuiContainer<GenericTileEntity> {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 198;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingcard.png");

    private BlockRender[] slots = new BlockRender[1 + INPUT_SLOTS];

    public GuiCraftingCard(CraftingCardContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, null, container, RFToolsControl.GUI_MANUAL_CONTROL, "craftingcard");
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        toplevel.addChild(new Label(mc, this).setText("Regular 3x3 crafting recipe").setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setLayoutHint(new PositionalLayout.PositionalHint(10, 4, 160, 14)));
        toplevel.addChild(new Label(mc, this).setText("or more complicated recipes").setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setLayoutHint(new PositionalLayout.PositionalHint(10, 17, 160, 14)));
        toplevel.addChild(new Button(mc, this)
                .setText("Update")
                .setTooltips("Update the item in the output", "slot to the recipe in the", "3x3 grid")
                .addButtonEvent(parent -> RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsControl.MODID, CommandHandler.CMD_TESTRECIPE, Arguments.EMPTY)))
                .setLayoutHint(new PositionalLayout.PositionalHint(110, 57, 60, 14)));
        ToggleButton toggle = new ToggleButton(mc, this)
                .setCheckMarker(true)
                .setText("NBT")
                .setTooltips("Enable this if you want", "opcodes like 'get_ingredients'", "to strictly match on NBT")
                .setLayoutHint(new PositionalLayout.PositionalHint(110, 74, 60, 14));
        ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        if (!heldItem.isEmpty()) {
            toggle.setPressed(CraftingCardItem.isStrictNBT(heldItem));
        }
        toggle.addButtonEvent(parent -> {
            RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemCard(new Argument("strictnbt", toggle.isPressed())));
        });

        toplevel.addChild(toggle);

        for (int y = 0 ; y < GRID_HEIGHT ; y++) {
            for (int x = 0 ; x < GRID_WIDTH ; x++) {
                int idx = y * GRID_WIDTH + x;
                createDummySlot(toplevel, idx, new PositionalLayout.PositionalHint(x * 18 + 10, y * 18 + 37, 18, 18), createSelectionEvent(idx));
            }
        }
        createDummySlot(toplevel, INPUT_SLOTS, new PositionalLayout.PositionalHint(10 + 8 * 18, 37, 18, 18), createSelectionEvent(INPUT_SLOTS));

        updateSlots();
        window = new Window(this, toplevel);
    }

    private void createDummySlot(Panel toplevel, int idx, PositionalLayout.PositionalHint hint, BlockRenderEvent selectionEvent) {
        slots[idx] = new BlockRender(mc, this) {
            @Override
            public List<String> getTooltips() {
                Object s = slots[idx].getRenderItem();
                if (s instanceof ItemStack) {
                    ItemStack stack = (ItemStack) s;
                    if (!stack.isEmpty()) {
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
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    return Collections.emptyList();
                }
            }
        }
                .setHilightOnHover(true)
                .setLayoutHint(hint);
        slots[idx].addSelectionEvent(selectionEvent);
        toplevel.addChild(slots[idx]);
    }

    private void updateSlots() {
        ItemStackList stacks = getStacks();
        if (stacks.isEmpty()) {
            return;
        }
        for (int i = 0 ; i < stacks.size() ; i++) {
            slots[i].setRenderItem(stacks.get(i));
        }
    }

    @Nonnull
    private ItemStackList getStacks() {
        ItemStack cardItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        ItemStackList stacks = ItemStackList.EMPTY;
        if (!cardItem.isEmpty() && cardItem.getItem() instanceof CraftingCardItem) {
            stacks = CraftingCardItem.getStacksFromItem(cardItem);
        }
        return stacks;
    }

    private BlockRenderEvent createSelectionEvent(final int idx) {
        return new BlockRenderEvent() {
            @Override
            public void select(Widget parent) {
                ItemStack itemstack = mc.player.inventory.getItemStack();
                slots[idx].setRenderItem(itemstack);
                ItemStackList stacks = getStacks();
                if (!stacks.isEmpty()) {
                    stacks.set(idx, itemstack);
                    ItemStack cardItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
                    CraftingCardItem.putStacksInItem(cardItem, stacks);
                    RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketItemNBTToServer(cardItem.getTagCompound()));
                }
            }

            @Override
            public void doubleClick(Widget parent) {

            }
        };
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateSlots();
        drawWindow();
    }
}
