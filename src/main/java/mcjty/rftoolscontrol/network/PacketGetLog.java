package mcjty.rftoolscontrol.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ByteBufConverter;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetLog extends PacketRequestListFromServer<PacketGetLog.StringConverter, PacketGetLog, PacketLogReady> {

    public PacketGetLog() {
    }

    public PacketGetLog(BlockPos pos) {
        super(RFToolsControl.MODID, pos, ProcessorTileEntity.CMD_GETLOG);
    }

    public static class Handler implements IMessageHandler<PacketGetLog, IMessage> {
        @Override
        public IMessage onMessage(PacketGetLog message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetLog message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<StringConverter> list = (List<StringConverter>) commandHandler.executeWithResultList(message.command, message.args);
            if (list == null) {
                Logging.log("Command " + message.command + " was not handled!");
                return;
            }
            RFToolsCtrlMessages.INSTANCE.sendTo(new PacketLogReady(message.pos, ProcessorTileEntity.CLIENTCMD_GETLOG, list), ctx.getServerHandler().playerEntity);
        }
    }

    public static class StringConverter implements ByteBufConverter {

        private final String message;

        public StringConverter(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkTools.writeString(buf, message);
        }
    }
}
