package mcjty.rftoolscontrol;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.network.PacketGraphicsReady;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;

public class CommandHandler {

    public static final String CMD_TESTRECIPE = "testRecipe";

    public static final String CMD_GETGRAPHICS = "getGraphics";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static void registerCommands() {
        McJtyLib.registerCommand(RFToolsControl.MODID, CMD_TESTRECIPE, (player, arguments) -> {
            ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return false;
            }
            if (heldItem.getItem() instanceof CraftingCardItem) {
                CraftingCardItem.testRecipe(player.getEntityWorld(), heldItem);
            }
            return true;
        });
        McJtyLib.registerCommand(RFToolsControl.MODID, CMD_GETGRAPHICS, (player, arguments) -> {
            TileEntity te = player.getEntityWorld().getTileEntity(arguments.get(PARAM_POS));
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                RFToolsCtrlMessages.INSTANCE.sendTo(new PacketGraphicsReady(processor), ((ServerPlayerEntity)player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
            return true;
        });
    }
}
