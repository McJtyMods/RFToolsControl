package mcjty.rftoolscontrol.setup;

public class GuiProxy {} /* @todo 1.15 implements IGuiHandler {
    public static final String SHIFT_MESSAGE = "<Press Shift>";

    private static int modGuiIndex = 0;
    public static final int GUI_TANK = modGuiIndex++;
    public static final int GUI_WORKBENCH = modGuiIndex++;
    public static final int GUI_CRAFTINGCARD = modGuiIndex++;
    public static final int GUI_CRAFTINGSTATION = modGuiIndex++;
    public static final int GUI_NODE = modGuiIndex++;
    public static final int GUI_PROCESSOR = modGuiIndex++;
    public static final int GUI_PROGRAMMER = modGuiIndex++;
    public static final int GUI_MANUAL_CONTROL = modGuiIndex++;

    @Override
    public Object getServerGuiElement(int guiid, PlayerEntity entityPlayer, World world, int x, int y, int z) {
        if (guiid == GUI_MANUAL_CONTROL) {
            return null;
        } else if (guiid == GUI_CRAFTINGCARD) {
            return new CraftingCardContainer(entityPlayer);
        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createServerContainer(entityPlayer, te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, PlayerEntity entityPlayer, World world, int x, int y, int z) {
        if (guiid == GUI_MANUAL_CONTROL) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_CONTROL);
        } else if (guiid == GUI_CRAFTINGCARD) {
            return new GuiCraftingCard(new CraftingCardContainer(entityPlayer));
        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createClientGui(entityPlayer, te);
        }
        return null;
    }
}
*/