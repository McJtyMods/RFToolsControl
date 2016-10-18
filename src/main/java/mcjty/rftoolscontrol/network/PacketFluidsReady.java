package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketListToClient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketFluidsReady extends PacketListToClient<PacketGetFluids.FluidEntry> {

    public PacketFluidsReady() {
    }

    public PacketFluidsReady(BlockPos pos, String command, List<PacketGetFluids.FluidEntry> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketFluidsReady, IMessage> {
        @Override
        public IMessage onMessage(PacketFluidsReady message, MessageContext ctx) {
            RFToolsControl.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketFluidsReady message, MessageContext ctx) {
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
    protected PacketGetFluids.FluidEntry createItem(ByteBuf buf) {
        FluidStack fluidStack = null;
        if (buf.readBoolean()) {
            fluidStack = NetworkTools.readFluidStack(buf);
        }
        boolean allocated = buf.readBoolean();
        return new PacketGetFluids.FluidEntry(fluidStack, allocated);
    }

    @Override
    protected void writeItemToBuf(ByteBuf buf, PacketGetFluids.FluidEntry item) {
        if (item == null) {
            buf.writeByte(-1);
        } else {
            if (item.getFluidStack() != null) {
                buf.writeBoolean(true);
                NetworkTools.writeFluidStack(buf, item.getFluidStack());
            } else {
                buf.writeBoolean(false);
            }
            buf.writeBoolean(item.isAllocated());
        }
    }
}
