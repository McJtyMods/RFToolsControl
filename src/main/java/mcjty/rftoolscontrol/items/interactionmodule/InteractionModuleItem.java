package mcjty.rftoolscontrol.items.interactionmodule;

import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.config.ConfigSetup;
import mcjty.rftoolscontrol.items.GenericRFToolsItem;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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


    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ConfigSetup.INTERACTMODULE_RFPERTICK.get() + " RF/tick");
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTagCompound();
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
            list.add(TextFormatting.YELLOW + "Sneak right-click on a processor to set the");
            list.add(TextFormatting.YELLOW + "target for this module");
        }
    }

    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundNBT tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
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