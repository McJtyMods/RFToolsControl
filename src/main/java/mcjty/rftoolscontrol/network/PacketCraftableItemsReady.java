package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketListToClient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.typed.Type;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketCraftableItemsReady extends PacketListToClient<ItemStack> {

    public PacketCraftableItemsReady() {
    }

    public PacketCraftableItemsReady(BlockPos pos, String command, List<ItemStack> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketCraftableItemsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketCraftableItemsReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCraftableItemsReady message, MessageContext ctx) {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list, Type.create(ItemStack.class))) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected ItemStack createItem(ByteBuf buf) {
        if (buf.readBoolean()) {
            return NetworkTools.readItemStack(buf);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, ItemStack s) {
        if (s.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            NetworkTools.writeItemStack(buf, s);
        }
    }
}
