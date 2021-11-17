package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class NetworkIdentifierItem extends Item implements ITooltipSettings {

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(header(),
                    gold(stack -> !ModuleTools.hasModuleTarget(stack)),
                    parameter("target", ModuleTools::hasModuleTarget, ModuleTools::getTargetString));

    public NetworkIdentifierItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(1)
                .tab(RFToolsControl.setup.getTab()));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<ITextComponent> list, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(itemStack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), itemStack, list, flag);
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }

        if (block == ProcessorModule.PROCESSOR.get()) {
            tagCompound.putString("monitordim", world.dimension().location().toString());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            if (world.isClientSide) {
                Logging.message(player, "Network identifier is set to block");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            if (world.isClientSide) {
                Logging.message(player, "Network identifier is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

}