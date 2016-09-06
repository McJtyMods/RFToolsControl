package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketVariablesReady extends PacketListFromServer<PacketVariablesReady,PacketGetVariables.ParameterConverter> {

    public PacketVariablesReady() {
    }

    public PacketVariablesReady(BlockPos pos, String command, List<PacketGetVariables.ParameterConverter> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketVariablesReady, IMessage> {
        @Override
        public IMessage onMessage(PacketVariablesReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketVariablesReady message, MessageContext ctx) {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list)) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected PacketGetVariables.ParameterConverter createItem(ByteBuf buf) {
        return new PacketGetVariables.ParameterConverter(Parameter.readFromBuf(buf));
    }
}
