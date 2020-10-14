package mcjty.rftoolscontrol.items;

import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NetworkIdentifierItem extends GenericRFToolsItem {

    public NetworkIdentifierItem() {
        super("network_identifier");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        
        String[] networkIdentifierI18N = I18n.format("tooltips." + RFToolsControl.MODID + "." + "network_identifier").split("0x0a");
        if (tagCompound != null) {
            if (tagCompound.hasKey("monitorx")) {
                int monitordim = tagCompound.getInteger("monitordim");
                int monitorx = tagCompound.getInteger("monitorx");
                int monitory = tagCompound.getInteger("monitory");
                int monitorz = tagCompound.getInteger("monitorz");
                list.add(TextFormatting.YELLOW + networkIdentifierI18N[0] + monitorx + "," + monitory + "," + monitorz);
                list.add(TextFormatting.YELLOW + "(" + networkIdentifierI18N[1] + monitordim + ")");
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(networkIdentifierI18N[2]);
            list.add(networkIdentifierI18N[3]);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }

        if (block == ModBlocks.processorBlock) {
            tagCompound.setInteger("monitordim", world.provider.getDimension());
            tagCompound.setInteger("monitorx", pos.getX());
            tagCompound.setInteger("monitory", pos.getY());
            tagCompound.setInteger("monitorz", pos.getZ());
            if (world.isRemote) {
                Logging.message(player, "Network identifier is set to block");
            }
        } else {
            tagCompound.removeTag("monitordim");
            tagCompound.removeTag("monitorx");
            tagCompound.removeTag("monitory");
            tagCompound.removeTag("monitorz");
            if (world.isRemote) {
                Logging.message(player, "Network identifier is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return EnumActionResult.SUCCESS;
    }

}