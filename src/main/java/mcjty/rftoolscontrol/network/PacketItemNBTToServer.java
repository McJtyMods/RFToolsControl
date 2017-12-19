package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This packet will update the held item NBT from client to server
 */
public class PacketItemNBTToServer implements IMessage {
    private NBTTagCompound tagCompound;

    @Override
    public void fromBytes(ByteBuf buf) {
        tagCompound = NetworkTools.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeTag(buf, tagCompound);
    }

    public PacketItemNBTToServer() {
    }

    public PacketItemNBTToServer(NBTTagCompound tagCompound) {
        this.tagCompound = tagCompound;
    }

    public static class Handler implements IMessageHandler<PacketItemNBTToServer, IMessage> {
        @Override
        public IMessage onMessage(PacketItemNBTToServer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketItemNBTToServer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            if (heldItem.getItem() instanceof ProgramCardItem) {
                heldItem.setTagCompound(message.tagCompound);
            } else if (heldItem.getItem() instanceof CraftingCardItem) {
                heldItem.setTagCompound(message.tagCompound);
            }
        }
    }
}