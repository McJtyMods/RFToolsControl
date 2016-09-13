package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketListToClient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketRequestsReady extends PacketListToClient<CraftingRequest> {

    public PacketRequestsReady() {
    }

    public PacketRequestsReady(BlockPos pos, String command, List<CraftingRequest> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketRequestsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestsReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestsReady message, MessageContext ctx) {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list)) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected CraftingRequest createItem(ByteBuf buf) {
        String id = NetworkTools.readString(buf);
        ItemStack stack = NetworkTools.readItemStack(buf);
        int amount = buf.readInt();
        CraftingRequest request = new CraftingRequest(id, stack, amount);
        request.setOk(buf.readLong());
        request.setFailed(buf.readLong());
        return request;
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, CraftingRequest s) {
        NetworkTools.writeString(buf, s.getTicket());
        NetworkTools.writeItemStack(buf, s.getStack());
        buf.writeInt(s.getTodo());
        buf.writeLong(s.getOk());
        buf.writeLong(s.getFailed());
    }
}
