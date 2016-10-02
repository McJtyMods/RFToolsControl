package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Set the recipe output for the current 3x3 grid
 */
public class PacketTestRecipe implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public PacketTestRecipe() {
    }

    public static class Handler implements IMessageHandler<PacketTestRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketTestRecipe message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTestRecipe message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = playerEntity.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem == null) {
                return;
            }
            if (heldItem.getItem() instanceof CraftingCardItem) {
                ((CraftingCardItem) heldItem.getItem()).testRecipe(playerEntity.getEntityWorld(), heldItem);
            }
        }
    }
}