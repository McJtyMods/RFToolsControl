package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ByteBufConverter;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetVariables extends PacketRequestListFromServer<PacketGetVariables.ParameterConverter, PacketGetVariables, PacketVariablesReady> {

    public PacketGetVariables() {
    }

    public PacketGetVariables(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETVARS);
    }

    public static class Handler implements IMessageHandler<PacketGetVariables, IMessage> {
        @Override
        public IMessage onMessage(PacketGetVariables message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetVariables message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<ParameterConverter> list = (List<ParameterConverter>) commandHandler.executeWithResultList(message.command, message.args);
            if (list == null) {
                Logging.log("Command " + message.command + " was not handled!");
                return;
            }
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketVariablesReady(message.pos, ProcessorTileEntity.CLIENTCMD_GETVARS, list), ctx.getServerHandler().playerEntity);
        }
    }

    public static class ParameterConverter implements ByteBufConverter {

        private final Parameter parameter;

        public ParameterConverter(Parameter parameter) {
            this.parameter = parameter;
        }

        public Parameter getParameter() {
            return parameter;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            if (parameter == null) {
                buf.writeByte(-1);
            } else {
                parameter.writeToBuf(buf);
            }
        }
    }
}
