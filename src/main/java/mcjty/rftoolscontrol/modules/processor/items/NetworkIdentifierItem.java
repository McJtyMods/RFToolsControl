package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class NetworkIdentifierItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(stack -> !ModuleTools.hasModuleTarget(stack)),
                    parameter("target", ModuleTools::hasModuleTarget, ModuleTools::getTargetString));

    public NetworkIdentifierItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, list, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), itemStack, list, flag);
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

        if (block == ProcessorModule.PROCESSOR.block().get()) {
            // Store a human-readable target (for tooltip) and position in the standard ItemModule component
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, Tools.getReadableName(world, pos));
            if (world.isClientSide) {
                Logging.message(player, "Network identifier is set to block");
            }
        } else {
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Network identifier is cleared");
            }
        }
        return InteractionResult.SUCCESS;
    }

}
