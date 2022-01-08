package mcjty.rftoolscontrol;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.network.PacketGraphicsReady;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkDirection;

public class CommandHandler {

    public static final String CMD_TESTRECIPE = "testRecipe";

    public static final String CMD_GETGRAPHICS = "getGraphics";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static void registerCommands() {
        McJtyLib.registerCommand(RFToolsControl.MODID, CMD_TESTRECIPE, (player, arguments) -> {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return false;
            }
            if (heldItem.getItem() instanceof CraftingCardItem) {
                CraftingCardItem.testRecipe(player.getCommandSenderWorld(), heldItem);
            }
            return true;
        });
        McJtyLib.registerCommand(RFToolsControl.MODID, CMD_GETGRAPHICS, (player, arguments) -> {
            BlockEntity te = player.getCommandSenderWorld().getBlockEntity(arguments.get(PARAM_POS));
            if (te instanceof ProcessorTileEntity processor) {
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketGraphicsReady(processor), ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
            return true;
        });
    }
}
