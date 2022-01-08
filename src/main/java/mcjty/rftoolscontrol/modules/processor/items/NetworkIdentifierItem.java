package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

import net.minecraft.world.item.Item.Properties;

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
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), itemStack, list, flag);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundTag();
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
        return InteractionResult.SUCCESS;
    }

}