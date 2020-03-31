package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class NetworkIdentifierItem extends Item {

    public NetworkIdentifierItem() {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(1)
                .group(RFToolsControl.setup.getTab()));

//        super((Properties) "network_identifier");
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(itemStack, worldIn, list, flagIn);
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("monitorx")) {
                String monitordim = tagCompound.getString("monitordim");
                int monitorx = tagCompound.getInt("monitorx");
                int monitory = tagCompound.getInt("monitory");
                int monitorz = tagCompound.getInt("monitorz");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Linked to processor at " + monitorx + "," + monitory + "," + monitorz));
                list.add(new StringTextComponent(TextFormatting.YELLOW + "(dimension  " + monitordim + ")"));
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(new StringTextComponent("Sneak right-click on a processor to set"));
            list.add(new StringTextComponent("the target for this identifier"));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = player.getHeldItem(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }

        if (block == ProcessorSetup.PROCESSOR.get()) {
            tagCompound.putString("monitordim", world.getDimension().getType().getRegistryName().toString());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            if (world.isRemote) {
                Logging.message(player, "Network identifier is set to block");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            if (world.isRemote) {
                Logging.message(player, "Network identifier is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

}