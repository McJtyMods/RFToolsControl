package mcjty.rftoolscontrol.items.interactionmodule;

import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.config.ConfigSetup;
import mcjty.rftoolscontrol.items.GenericRFToolsItem;
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

public class InteractionModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public InteractionModuleItem() {
        super("interaction_module");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<InteractionScreenModule> getServerScreenModule() {
        return InteractionScreenModule.class;
    }

    @Override
    public Class<InteractionClientScreenModule> getClientScreenModule() {
        return InteractionClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "INT";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl()
                .label("Signal:").text("signal", "Signal name").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ConfigSetup.INTERACTMODULE_RFPERTICK.get() + " RF/tick");
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            if (tagCompound.hasKey("monitorx")) {
                int monitorx = tagCompound.getInteger("monitorx");
                int monitory = tagCompound.getInteger("monitory");
                int monitorz = tagCompound.getInteger("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")");
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            String[] interactionModuleI18n = I18n.format("tooltips." + RFToolsControl.MODID + "." + "interaction_module").split("0x0a");
            for (String str : interactionModuleI18n){
                list.add(TextFormatting.YELLOW + str);
            }
            
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
                Logging.message(player, "Interaction module is set to block");
            }
        } else {
            tagCompound.removeTag("monitordim");
            tagCompound.removeTag("monitorx");
            tagCompound.removeTag("monitory");
            tagCompound.removeTag("monitorz");
            if (world.isRemote) {
                Logging.message(player, "Interaction module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return EnumActionResult.SUCCESS;
    }

}