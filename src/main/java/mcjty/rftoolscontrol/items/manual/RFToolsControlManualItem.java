package mcjty.rftoolscontrol.items.manual;

import mcjty.rftoolscontrol.items.GenericRFToolsItem;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class RFToolsControlManualItem extends GenericRFToolsItem {

    public RFToolsControlManualItem() {
        super("rftoolscontrol_manual");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(RFToolsControl.instance, RFToolsControl.GUI_MANUAL_CONTROL, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}
