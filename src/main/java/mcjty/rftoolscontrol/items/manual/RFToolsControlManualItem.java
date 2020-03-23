package mcjty.rftoolscontrol.items.manual;

public class RFToolsControlManualItem {} /* @todo 1.15 extends GenericRFToolsItem {

    public RFToolsControlManualItem() {
        super("rftoolscontrol_manual");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            player.openGui(RFToolsControl.instance, GuiProxy.GUI_MANUAL_CONTROL, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}
*/