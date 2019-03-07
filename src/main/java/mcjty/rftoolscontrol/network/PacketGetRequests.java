package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingRequest;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetRequests implements IMessage {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetRequests() {
    }

    public PacketGetRequests(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public static class Handler implements IMessageHandler<PacketGetRequests, IMessage> {

        @Override
        public IMessage onMessage(mcjty.rftoolscontrol.network.PacketGetRequests message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(mcjty.rftoolscontrol.network.PacketGetRequests message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<CraftingRequest> list = commandHandler.executeWithResultList(CraftingStationTileEntity.CMD_GETREQUESTS, message.params, Type.create(CraftingRequest.class));
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketRequestsReady(message.pos, CraftingStationTileEntity.CLIENTCMD_GETREQUESTS, list), ctx.getServerHandler().player);
        }
    }
}
