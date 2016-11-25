package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketListToClient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketTankFluidsReady extends PacketListToClient<FluidStack> {

    public PacketTankFluidsReady() {
    }

    public PacketTankFluidsReady(BlockPos pos, String command, List<FluidStack> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketTankFluidsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketTankFluidsReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTankFluidsReady message, MessageContext ctx) {
            TileEntity te = RFToolsControl.proxy.getClientWorld().getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list, Type.create(FluidStack.class))) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected FluidStack createItem(ByteBuf buf) {
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            fluidStack = NetworkTools.readFluidStack(buf);
        }
        return fluidStack;
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, FluidStack item) {
        if (item == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            NetworkTools.writeFluidStack(buf, item);
        }
    }
}
